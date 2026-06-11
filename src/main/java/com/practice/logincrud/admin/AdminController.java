package com.practice.logincrud.admin;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    //관리자 페이지 이동
    @GetMapping("/admin")
    public String admin() {
        return "/admin/admin-login";
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
}
