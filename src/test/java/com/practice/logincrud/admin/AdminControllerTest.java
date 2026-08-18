package com.practice.logincrud.admin;

import com.practice.logincrud.member.email.EmailAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.Model;
import org.springframework.ui.ExtendedModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 관리자 로그인 2단계(이메일 인증코드) 흐름 검증.
 * 실제 DB/메일 발송 없이 AdminService, EmailAuthService를 mock으로 대체해 컨트롤러 분기 로직만 검증한다.
 */
class AdminControllerTest {

    private AdminService adminService;
    private EmailAuthService emailAuthService;
    private AdminController adminController;
    private MockHttpSession session;

    private static final String EMAIL = "admin@example.com";

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        emailAuthService = mock(EmailAuthService.class);
        adminController = new AdminController(adminService, emailAuthService);
        session = new MockHttpSession();
    }

    private AdminDto adminDto() {
        AdminDto dto = new AdminDto();
        dto.setId(1L);
        dto.setEmail(EMAIL);
        dto.setNickname("관리자");
        dto.setRole("ADMIN");
        return dto;
    }

    @Test
    void 비번_인증_성공하면_바로_로그인되지_않고_인증코드_단계로_이동한다() {
        when(adminService.adminAccess(EMAIL, "pw1234")).thenReturn(adminDto());

        String view = adminController.login(EMAIL, "pw1234", session);

        assertThat(view).isEqualTo("redirect:/admin/verify");
        assertThat(session.getAttribute("role")).isNull();
        assertThat(session.getAttribute("pendingAdminEmail")).isEqualTo(EMAIL);
        verify(emailAuthService).sendCode(eq(EMAIL), eq(session));
    }

    @Test
    void 비번이_틀리면_인증코드를_보내지_않고_에러로_돌아간다() {
        when(adminService.adminAccess(EMAIL, "wrong")).thenReturn(null);

        String view = adminController.login(EMAIL, "wrong", session);

        assertThat(view).isEqualTo("redirect:/admin?error=true");
        assertThat(session.getAttribute("pendingAdminEmail")).isNull();
        verify(emailAuthService, never()).sendCode(anyString(), any());
    }

    @Test
    void 승인_대기_계정이면_인증코드를_보내지_않고_승인대기_안내로_돌아간다() {
        when(adminService.adminAccess(EMAIL, "pw1234"))
                .thenThrow(new IllegalStateException("승인 대기 중인 계정입니다."));

        String view = adminController.login(EMAIL, "pw1234", session);

        assertThat(view).isEqualTo("redirect:/admin?pending=true");
        assertThat(session.getAttribute("pendingAdminEmail")).isNull();
        verify(emailAuthService, never()).sendCode(anyString(), any());
    }

    @Test
    void 인증코드가_맞으면_세션에_관리자_정보가_세팅되고_대시보드로_이동한다() {
        session.setAttribute("pendingAdminEmail", EMAIL);
        when(emailAuthService.verifyCode(EMAIL, "123456", session)).thenReturn(true);
        when(adminService.findByEmail(EMAIL)).thenReturn(adminDto());

        String view = adminController.verify("123456", session, new ExtendedModelMap());

        assertThat(view).isEqualTo("redirect:/admin/dashboard");
        assertThat(session.getAttribute("role")).isEqualTo("ADMIN");
        assertThat(session.getAttribute("email")).isEqualTo(EMAIL);
        assertThat(session.getAttribute("pendingAdminEmail")).isNull();
        verify(emailAuthService).clear(EMAIL, session);
    }

    @Test
    void 인증코드가_틀리면_세션에_role이_세팅되지_않고_인증폼을_다시_보여준다() {
        session.setAttribute("pendingAdminEmail", EMAIL);
        when(emailAuthService.verifyCode(EMAIL, "000000", session)).thenReturn(false);
        Model model = new ExtendedModelMap();

        String view = adminController.verify("000000", session, model);

        assertThat(view).isEqualTo("admin/admin-verify");
        assertThat(session.getAttribute("role")).isNull();
        // 브루트포스 방지는 EmailAuthService가 이미 처리하므로 재시도를 위해 pendingAdminEmail은 유지한다
        assertThat(session.getAttribute("pendingAdminEmail")).isEqualTo(EMAIL);
        assertThat(model.getAttribute("error")).isNotNull();
    }

    @Test
    void 인증_단계를_거치지_않고_직접_verify_페이지에_접근하면_로그인_페이지로_리다이렉트된다() {
        String view = adminController.verifyForm(session, new ExtendedModelMap());

        assertThat(view).isEqualTo("redirect:/admin");
    }

    @Test
    void 이메일_인증을_안_하면_가입_신청이_거부된다() {
        when(emailAuthService.isVerified(EMAIL, session)).thenReturn(false);
        Model model = new ExtendedModelMap();

        String view = adminController.join(EMAIL, "pw1234", "pw1234", "닉네임", model, session);

        assertThat(view).isEqualTo("admin/admin-create");
        assertThat(model.getAttribute("error")).isNotNull();
        verify(adminService, never()).joinAdmin(anyString(), anyString(), anyString());
    }

    @Test
    void 비번확인이_다르면_DB에_저장되지_않고_거부된다() {
        when(emailAuthService.isVerified(EMAIL, session)).thenReturn(true);
        Model model = new ExtendedModelMap();

        String view = adminController.join(EMAIL, "pw1234", "다른비번", "닉네임", model, session);

        assertThat(view).isEqualTo("admin/admin-create");
        assertThat(model.getAttribute("error")).isNotNull();
        verify(adminService, never()).joinAdmin(anyString(), anyString(), anyString());
    }

    @Test
    void 가입_신청이_성공하면_승인대기로_저장되고_인증정보가_정리된다() {
        when(emailAuthService.isVerified(EMAIL, session)).thenReturn(true);
        when(adminService.joinAdmin(EMAIL, "pw1234", "닉네임")).thenReturn(true);
        Model model = new ExtendedModelMap();

        String view = adminController.join(EMAIL, "pw1234", "pw1234", "닉네임", model, session);

        assertThat(view).isEqualTo("redirect:/admin?joinRequested=true");
        verify(adminService).joinAdmin(EMAIL, "pw1234", "닉네임");
        verify(emailAuthService).clear(EMAIL, session);
    }

    @Test
    void 최고관리자가_아니면_승인_목록에_접근할_수_없다() {
        session.setAttribute("role", "ADMIN");

        String view = adminController.approvals(session, new ExtendedModelMap());

        assertThat(view).isEqualTo("redirect:/admin/dashboard");
        verify(adminService, never()).findPendingAdmins();
    }

    @Test
    void 최고관리자는_승인_목록에_접근할_수_있다() {
        session.setAttribute("role", "2");
        when(adminService.findPendingAdmins()).thenReturn(java.util.List.of());

        String view = adminController.approvals(session, new ExtendedModelMap());

        assertThat(view).isEqualTo("admin/admin-approvals");
        verify(adminService).findPendingAdmins();
    }

    @Test
    void 최고관리자가_승인하면_서비스에_승인_처리가_위임된다() {
        session.setAttribute("role", "2");

        String view = adminController.approve(1L, session);

        assertThat(view).isEqualTo("redirect:/admin/approvals");
        verify(adminService).approveAdmin(1L);
    }

    @Test
    void 일반_관리자는_승인_처리를_할_수_없다() {
        session.setAttribute("role", "ADMIN");

        String view = adminController.approve(1L, session);

        assertThat(view).isEqualTo("redirect:/admin/dashboard");
        verify(adminService, never()).approveAdmin(anyLong());
    }
}
