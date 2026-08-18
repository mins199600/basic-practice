package com.practice.logincrud.admin;
import com.practice.logincrud.member.email.EmailAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private static final String PENDING_ADMIN_EMAIL = "pendingAdminEmail";

    private final AdminService adminService;
    private final EmailAuthService emailAuthService;

    //관리자 페이지 이동
    @GetMapping("/admin")
    public String admin(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String pending,
                        @RequestParam(required = false) String joinRequested,
                        @RequestParam(required = false) String passwordChanged,
                        Model model) {
        if (error != null) {
            model.addAttribute("loginError", "아이디 또는 비밀번호가 일치하지 않습니다.");
        }
        if (pending != null) {
            model.addAttribute("loginError", "승인 대기 중인 계정입니다. 최고관리자 승인 후 로그인할 수 있습니다.");
        }
        if (joinRequested != null) {
            model.addAttribute("infoMessage", "가입 신청이 접수되었습니다. 최고관리자 승인 후 로그인할 수 있습니다.");
        }
        if (passwordChanged != null) {
            model.addAttribute("infoMessage", "비밀번호가 변경되었습니다. 다시 로그인해주세요.");
        }
        return "admin/admin-login";
    }

    //관리자 회원가입 페이지 이동
    @GetMapping("/admin/join")
    public String join() {
        return "admin/admin-create";
    }

    //관리자 가입 - 이메일 인증코드 발송 (이미 가입/신청된 이메일이면 차단)
    @PostMapping("/admin/join/send-code")
    @ResponseBody
    public Map<String, Object> sendJoinCode(@RequestParam String email, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        if (adminService.isEmailTaken(email)) {
            log.warn("관리자 가입 인증코드 발송 차단 - 이미 사용 중인 이메일 email={}", email);
            result.put("success", false);
            result.put("message", "이미 가입되었거나 승인 대기 중인 이메일입니다.");
            return result;
        }

        try {
            emailAuthService.sendCode(email, session);
            result.put("success", true);
            result.put("message", "인증번호를 발송했습니다.");
        } catch (Exception e) {
            log.error("관리자 가입 인증코드 메일 발송 실패 email={}", email, e);
            result.put("success", false);
            result.put("message", "메일 발송 실패: 메일 설정을 확인하세요.");
        }
        return result;
    }

    //관리자 가입 - 이메일 인증코드 확인
    @PostMapping("/admin/join/verify-code")
    @ResponseBody
    public Map<String, Object> verifyJoinCode(@RequestParam String email,
                                              @RequestParam String code,
                                              HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        boolean ok = emailAuthService.verifyCode(email, code, session);
        result.put("success", ok);
        result.put("message", ok ? "이메일 인증 완료" : "인증번호가 올바르지 않거나 만료되었습니다.");
        return result;
    }

    //관리자 가입 신청 - 이메일 인증 완료 + 비번 확인 일치 시에만 승인대기(PENDING_ADMIN) 상태로 저장
    @PostMapping("/admin/join")
    public String join(@RequestParam String email,
                       @RequestParam String password,
                       @RequestParam String passwordCheck,
                       @RequestParam(required = false) String nickName,
                       Model model,
                       HttpSession session) {

        if (!emailAuthService.isVerified(email, session)) {
            log.warn("관리자 가입 차단 - 이메일 미인증 email={}", email);
            model.addAttribute("error", "이메일 인증을 먼저 완료해주세요.");
            return "admin/admin-create";
        }

        if (!password.equals(passwordCheck)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "admin/admin-create";
        }

        boolean joined = adminService.joinAdmin(email, password, nickName);
        if (!joined) {
            model.addAttribute("error", "가입 신청에 실패했습니다. 잠시 후 다시 시도해주세요.");
            return "admin/admin-create";
        }

        emailAuthService.clear(email, session);
        log.info("관리자 가입 신청 완료 - 승인 대기 email={}", email);
        return "redirect:/admin?joinRequested=true";
    }

    //최고관리자 전용 - 승인 대기 중인 관리자 목록
    @GetMapping("/admin/approvals")
    public String approvals(HttpSession session, Model model) {
        if (!isSuperAdmin(session)) {
            return "redirect:/admin/dashboard";
        }
        List<AdminDto> pendingAdmins = adminService.findPendingAdmins();
        model.addAttribute("pendingAdmins", pendingAdmins);
        return "admin/admin-approvals";
    }

    //최고관리자 전용 - 관리자 승인
    @PostMapping("/admin/approvals/{id}/approve")
    public String approve(@PathVariable Long id, HttpSession session) {
        if (!isSuperAdmin(session)) {
            return "redirect:/admin/dashboard";
        }
        adminService.approveAdmin(id);
        log.info("관리자 승인 처리 id={}", id);
        return "redirect:/admin/approvals";
    }

    //최고관리자 전용 - 관리자 승인 거부
    @PostMapping("/admin/approvals/{id}/reject")
    public String reject(@PathVariable Long id, HttpSession session) {
        if (!isSuperAdmin(session)) {
            return "redirect:/admin/dashboard";
        }
        adminService.rejectAdmin(id);
        log.info("관리자 승인 거부 처리 id={}", id);
        return "redirect:/admin/approvals";
    }

    //세션의 role이 정확히 최고관리자(2)인지 확인 - AdminInterceptor는 ADMIN/2를 둘 다 통과시키므로 승인 화면은 컨트롤러에서 별도로 좁혀야 함
    private boolean isSuperAdmin(HttpSession session) {
        return AdminRole.fromRaw(session.getAttribute("role")) == AdminRole.SUPER_ADMIN;
    }

    //관리자 로그인 1단계 - 이메일/비번 검증. 성공해도 바로 로그인시키지 않고 이메일 인증코드 단계로 넘긴다.
    @PostMapping("/admin/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession httpSession
    ) {
        AdminDto adminLogin;
        try {
            adminLogin = adminService.adminAccess(email, password);
        } catch (IllegalStateException e) {
            log.info("로그인 실패 - {}", e.getMessage());
            return "redirect:/admin?pending=true";
        }

        if (adminLogin != null) {
            httpSession.setAttribute(PENDING_ADMIN_EMAIL, adminLogin.getEmail());
            emailAuthService.sendCode(adminLogin.getEmail(), httpSession);

            log.info("관리자 1차 인증 성공 - 이메일 인증코드 발송 email={}", adminLogin.getEmail());
            return "redirect:/admin/verify";

        } else {
            log.info("로그인 실패");
            return "redirect:/admin?error=true";
        }
    }

    //관리자 로그인 2단계 - 이메일 인증코드 입력 폼
    @GetMapping("/admin/verify")
    public String verifyForm(HttpSession httpSession, Model model) {
        String pendingEmail = (String) httpSession.getAttribute(PENDING_ADMIN_EMAIL);
        if (pendingEmail == null) {
            // 1단계(이메일/비번 검증)를 거치지 않고 직접 URL로 들어온 경우 차단
            return "redirect:/admin";
        }

        model.addAttribute("email", pendingEmail);
        return "admin/admin-verify";
    }

    //관리자 로그인 2단계 - 이메일 인증코드 검증, 성공 시 최종 로그인 완료
    @PostMapping("/admin/verify")
    public String verify(@RequestParam String code, HttpSession httpSession, Model model) {
        String pendingEmail = (String) httpSession.getAttribute(PENDING_ADMIN_EMAIL);
        if (pendingEmail == null) {
            return "redirect:/admin";
        }

        boolean verified = emailAuthService.verifyCode(pendingEmail, code, httpSession);
        if (!verified) {
            log.info("관리자 2차 인증 실패 email={}", pendingEmail);
            model.addAttribute("email", pendingEmail);
            model.addAttribute("error", "인증번호가 올바르지 않거나 만료되었습니다.");
            return "admin/admin-verify";
        }

        AdminDto admin = adminService.findByEmail(pendingEmail);
        httpSession.setAttribute("memberId", admin.getId());
        httpSession.setAttribute("email", admin.getEmail());
        httpSession.setAttribute("role", admin.getRole());
        httpSession.setAttribute("nickName", admin.getNickname());

        httpSession.removeAttribute(PENDING_ADMIN_EMAIL);
        emailAuthService.clear(pendingEmail, httpSession);

        log.info("관리자 로그인 성공 email={}", pendingEmail);
        return "redirect:/admin/dashboard";
    }

    //아이디 찾기
    @GetMapping("/admin/find-id")
    public String findIdForm() {
        return "admin/find-id";
    }

    //아이디 찾기
    @PostMapping("/admin/find-id")
    public String findId(@RequestParam String nickname, Model model) {
        String email = adminService.findEmail(nickname);

        if (email != null) {
            model.addAttribute("foundEmail", email);
        } else {
            model.addAttribute("error", "일치하는 계정이 없습니다.");
        }
        return "admin/find-id";
    }

    //비밀번호 찾기
    @GetMapping("/admin/find-password")
    public String findPasswordForm() {
        return "admin/find-password";
    }

    @PostMapping("/admin/find-password")
    public String findPassword(@RequestParam String email,
                               @RequestParam String nickname,
                               Model model) {
        boolean verified = adminService.verifyAdmin(email, nickname);

        if (verified) {
            // 본인 확인 성공 → 비밀번호 재설정 페이지로
            model.addAttribute("verifiedEmail", email);
            return "admin/reset-password";
        } else {
            model.addAttribute("error", "이메일 또는 닉네임이 일치하지 않습니다.");
            return "admin/find-password";
        }
    }

    //비밀번호 재설정
    @PostMapping("/admin/reset-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String newPassword,
                                @RequestParam String newPasswordCheck,
                                Model model) {
        if (!newPassword.equals(newPasswordCheck)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            model.addAttribute("verifiedEmail", email);
            return "admin/reset-password";
        }

        adminService.updatePassword(email, newPassword);
        return "redirect:/admin?passwordChanged=true";
    }
}
