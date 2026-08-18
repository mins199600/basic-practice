package com.practice.logincrud.admin;

import com.practice.logincrud.member.email.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 관리자 가입 신청(승인 대기) 로직 검증.
 * 실제 DB/메일 발송 없이 AdminMapper, EmailSender를 mock으로 대체한다.
 */
class AdminServiceTest {

    private AdminMapper adminMapper;
    private EmailSender emailSender;
    private AdminService adminService;

    private static final String EMAIL = "requester@example.com";

    @BeforeEach
    void setUp() {
        adminMapper = mock(AdminMapper.class);
        emailSender = mock(EmailSender.class);
        // 실제 BCrypt 인코더를 그대로 사용 - 암호화 여부만 확인하면 되므로 mock할 필요 없음
        adminService = new AdminService(adminMapper, new BCryptPasswordEncoder(), emailSender);
    }

    @Test
    void 가입_신청하면_PENDING_ADMIN_상태로_저장된다() {
        when(adminMapper.insertAdmin(any())).thenReturn(1);
        when(adminMapper.findSuperAdminEmails()).thenReturn(List.of());

        boolean result = adminService.joinAdmin(EMAIL, "pw1234", "닉네임");

        assertThat(result).isTrue();
        ArgumentCaptor<AdminDto> captor = ArgumentCaptor.forClass(AdminDto.class);
        verify(adminMapper).insertAdmin(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo("PENDING_ADMIN");
        assertThat(captor.getValue().getPassword()).isNotEqualTo("pw1234"); // 평문 저장 금지
    }

    @Test
    void 가입_신청이_성공하면_등록된_모든_최고관리자에게_승인요청_메일이_간다() {
        when(adminMapper.insertAdmin(any())).thenReturn(1);
        when(adminMapper.findSuperAdminEmails()).thenReturn(List.of("super1@example.com", "super2@example.com"));

        adminService.joinAdmin(EMAIL, "pw1234", "닉네임");

        verify(emailSender).sendAdminApprovalRequest("super1@example.com", EMAIL);
        verify(emailSender).sendAdminApprovalRequest("super2@example.com", EMAIL);
    }

    @Test
    void DB_저장에_실패하면_최고관리자에게_메일을_보내지_않는다() {
        when(adminMapper.insertAdmin(any())).thenReturn(0);

        boolean result = adminService.joinAdmin(EMAIL, "pw1234", "닉네임");

        assertThat(result).isFalse();
        verify(adminMapper, never()).findSuperAdminEmails();
        verify(emailSender, never()).sendAdminApprovalRequest(anyString(), anyString());
    }

    @Test
    void 최고관리자가_한명도_없으면_메일_발송_없이_조용히_넘어간다() {
        when(adminMapper.insertAdmin(any())).thenReturn(1);
        when(adminMapper.findSuperAdminEmails()).thenReturn(List.of());

        boolean result = adminService.joinAdmin(EMAIL, "pw1234", "닉네임");

        assertThat(result).isTrue();
        verify(emailSender, never()).sendAdminApprovalRequest(anyString(), anyString());
    }

    @Test
    void 승인하면_매퍼에_위임되고_영향row가_1이면_true를_반환한다() {
        when(adminMapper.approveAdmin(1L)).thenReturn(1);

        assertThat(adminService.approveAdmin(1L)).isTrue();
    }

    @Test
    void 이미_처리된_승인_대상이면_false를_반환한다() {
        when(adminMapper.approveAdmin(1L)).thenReturn(0);

        assertThat(adminService.approveAdmin(1L)).isFalse();
    }

    private AdminDto accountWithRole(String role, String rawPassword) {
        AdminDto dto = new AdminDto();
        dto.setEmail(EMAIL);
        dto.setPassword(new BCryptPasswordEncoder().encode(rawPassword));
        dto.setRole(role);
        return dto;
    }

    @Test
    void 승인된_관리자는_비번이_맞으면_로그인_성공한다() {
        when(adminMapper.findAccountForLogin(EMAIL)).thenReturn(accountWithRole("ADMIN", "pw1234"));

        AdminDto result = adminService.adminAccess(EMAIL, "pw1234");

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void 최고관리자도_비번이_맞으면_로그인_성공한다() {
        when(adminMapper.findAccountForLogin(EMAIL)).thenReturn(accountWithRole("2", "pw1234"));

        AdminDto result = adminService.adminAccess(EMAIL, "pw1234");

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo("2");
    }

    @Test
    void 승인대기_상태는_비번이_맞아도_예외를_던지며_로그인이_차단된다() {
        when(adminMapper.findAccountForLogin(EMAIL)).thenReturn(accountWithRole("PENDING_ADMIN", "pw1234"));

        assertThatThrownBy(() -> adminService.adminAccess(EMAIL, "pw1234"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 비번이_틀리면_승인대기_상태여도_예외가_아니라_그냥_null을_반환한다() {
        when(adminMapper.findAccountForLogin(EMAIL)).thenReturn(accountWithRole("PENDING_ADMIN", "pw1234"));

        AdminDto result = adminService.adminAccess(EMAIL, "wrong-password");

        assertThat(result).isNull();
    }

    @Test
    void 존재하지_않는_계정은_null을_반환한다() {
        when(adminMapper.findAccountForLogin(EMAIL)).thenReturn(null);

        assertThat(adminService.adminAccess(EMAIL, "pw1234")).isNull();
    }
}
