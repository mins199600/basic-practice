package com.practice.logincrud.member;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class MemberController {

    @Autowired
    MemberService memberService;

    @GetMapping("/")
    public String home() {
        return "main";
    }

    //로그인
    @PostMapping("/login")
    public String login(HttpSession httpSession, @RequestParam String email, @RequestParam String password) {
        boolean success = memberService.userLogin(email, password);

        if (success) {
            httpSession.setAttribute("email", email);
            log.info("로그인 성공");
            return "home";
        } else {
            httpSession.setAttribute("error", "아이디 또는 비밀번호가 틀렸습니다");
            log.info("로그인 실패");
            return "main";
        }
    }

    //로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession httpSession) {
        httpSession.invalidate();
        log.info("로그아웃");
        return "redirect:/";
    }

    @GetMapping("/api/user-info")
    @ResponseBody
    public Map<String, String> getUserInfo(HttpSession session) {
        String email = (String) session.getAttribute("email");
        Map<String, String> response = new HashMap<>();
        response.put("email", email != null ? email : "로그인 안 함");
        return response;
    }

    //회원가입 페이지 이동
    @GetMapping("/join")
    public String join() {
        return "signup";
    }

    //회원가입 로직
    @PostMapping("/signup")
    public String signup(@RequestParam String email, @RequestParam String password) {
       boolean result = memberService.join(email, password);
       log.info("회원가입 로직 지나가요~~");

       if(result) {
           log.info("회원가입 성공");
           return "redirect:/";
       }else {
           log.info("회원가입 오류");
           return "signup";
       }

    }


}
