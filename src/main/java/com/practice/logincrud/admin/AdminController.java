package com.practice.logincrud.admin;
import com.practice.logincrud.member.email.EmailAuthService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private static final String PENDING_ADMIN_EMAIL = "pendingAdminEmail";

    private final AdminService adminService;
    private final EmailAuthService emailAuthService;

    //관리자 페이지 이동
    @GetMapping("/admin")
    public String admin() {
        return "admin/admin-login";
    }

    //관리자 회원가입 페이지 이동
    @GetMapping("/admin/join")
    public String join() {
        return "admin/admin-create";
    }

    //관리자 회워가입
    @PostMapping("/admin/join")
    public String join(@RequestParam String email,
                       @RequestParam String password,
                       @RequestParam String passwordCheck,
                       @RequestParam(required = false) String nickName,
                       Model model) {
        boolean adminResult = adminService.joinAdmin(email, password, nickName);

        if (!password.equals(passwordCheck)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "admin/admin-create";
        }

        if (adminResult) {
            return "redirect:/admin";
        } else {
            return "/admin/admin-create";
        }
    }

    //관리자 로그인 1단계 - 이메일/비번 검증. 성공해도 바로 로그인시키지 않고 이메일 인증코드 단계로 넘긴다.
    @PostMapping("/admin/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession httpSession
    ) {
        AdminDto adminLogin = adminService.adminAccess(email, password);

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
