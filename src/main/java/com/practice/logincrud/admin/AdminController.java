package com.practice.logincrud.admin;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

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
    public String join(AdminDto adminDto) {
        adminDto.setRole("ADMIN");
        adminService.joinAdmin(adminDto);
        return "redirect:/admin";
    }
}
