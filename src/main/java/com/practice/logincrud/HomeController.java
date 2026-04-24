package com.practice.logincrud;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class HomeController {

    @Autowired
    HomeService homeService;

    @GetMapping("/main")
    public String home() {
        return "redirect:/main.html";
    }

    @PostMapping("/login")
    public String login(Model model, @RequestParam String userId, @RequestParam String userPw) {
        boolean success = homeService.userLogin(userId, userPw);

        if (success) {
            model.addAttribute("userId", userId);
            log.info("login success");
            return "redirect:/success.html";
        } else {
            model.addAttribute("error", "아이디 또는 비밀번호가 틀렸습니다");
            log.info("로그인 실패");
            return "redirect:/main.html";
        }
    }

}
