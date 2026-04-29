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
    public String login(HttpSession httpSession, @RequestParam(required = false) String email, @RequestParam(required = false) String password) {
        log.info("login email: {}", email);
        log.info("login password: {}", password);
        boolean success = homeService.userLogin(email, password);

        if (success) {
            httpSession.setAttribute("email", email);
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
        String email = (String) httpSession.getAttribute("email");
        httpSession.invalidate();
        log.info("로그아웃");
        return "redirect:/main.html";
    }
    //이메일 중복 확인
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
        return "redirect:/signup.html";
    }

    //회원가입 로직
    @PostMapping("/signup")
    @ResponseBody
    public String signup(@RequestParam String email, @RequestParam String password) {
       boolean result = homeService.join(email, password);
       log.info("회원가입 로직 지나가요~~");
       if(!result) {
           return "<script>alert('이미 사용 중인 이메일입니다.'); location.href='/signup.html';</script>";
       }else {
           return "<script>alert('회원가입이 완료되었습니다.'); location.href='/main.html';</script>";
       }
    }

    //회원가입 수정
    @PostMapping("/user/edit")
    @ResponseBody
    public String editUser(@RequestParam String email,
                           @RequestParam String password,
                           HttpSession httpSession) {
        String updateUser = (String) httpSession.getAttribute("email");

        //null 체크 추가
        if (updateUser == null) {
            return "<script>alert('로그인이 필요합니다.'); location.href='/main.html';</script>";
        }

        homeService.editUser(updateUser, email, password);

        // 이메일 변경했으면 세션도 업데이트
        if (!updateUser.equals(email)) {
            httpSession.setAttribute("email", email);
        }

        return "<script>alert('회원정보가 수정되었습니다.'); location.href='/view.html';</script>";
    }


}



