package com.practice.logincrud.admin;
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

    private final AdminService adminService;

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

    //관리자 로그인
    @PostMapping("/admin/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession httpSession
    ) {
        AdminDto adminLogin = adminService.adminAccess(email, password);

        if (adminLogin != null) {
            httpSession.setAttribute("memberId", adminLogin.getId());
            httpSession.setAttribute("email", adminLogin.getEmail());
            httpSession.setAttribute("role", adminLogin.getRole());
            httpSession.setAttribute("nickName", adminLogin.getNickname());

            log.info("로그인 성공");
            return "redirect:/admin/dashboard";

        } else {
            log.info("로그인 실패");
            return "redirect:/admin?error=true";
        }
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
