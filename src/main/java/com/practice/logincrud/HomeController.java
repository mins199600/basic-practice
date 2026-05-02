package com.practice.logincrud;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class HomeController {

    @Autowired
    HomeService homeService;

    @GetMapping("/main")
    public String home() {
        return "redirect:/main.html";
    }

    //로그인
    @PostMapping("/login")
    public String login(HttpSession httpSession, @RequestParam String userId, @RequestParam String userPw) {
        boolean success = homeService.userLogin(userId, userPw);

        if (success) {
            httpSession.setAttribute("userId", userId);
            log.info("로그인 성공");
            return "redirect:/success.html";
        } else {
            httpSession.setAttribute("error", "아이디 또는 비밀번호가 틀렸습니다");
            log.info("로그인 실패");
            return "redirect:/main.html";
        }
    }

    //로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession httpSession) {
        String userId = (String) httpSession.getAttribute("userId");
        httpSession.invalidate();
        log.info("로그아웃");
        return "redirect:/main.html";
    }

    @GetMapping("/api/user-info")
    @ResponseBody
    public Map<String, String> getUserInfo(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        Map<String, String> response = new HashMap<>();
        response.put("userId", userId != null ? userId : "로그인 안 함");
        return response;
    }

    //회원가입 페이지 이동
    @GetMapping("/join")
    public String join() {
        return "redirect:/signup.html";
    }

    //회원가입 로직
    @PostMapping("/signup")
    @ResponseBody
    public String signup(@RequestParam String email, @RequestParam String password, Model model) {
       boolean result = homeService.join(email, password);
       log.info("회원가입 로직 지나가요~~");

       if(!result) {
           return "<script>alert('이미 사용 중인 이메일입니다.'); location.href='/signup.html';</script>";
       }else {
           return "<script>alert('회원가입이 완료되었습니다.'); location.href='/success.html';</script>";
       }

    }


}
