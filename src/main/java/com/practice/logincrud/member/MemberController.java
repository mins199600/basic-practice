package com.practice.logincrud.member;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String login(HttpSession httpSession,
                        @RequestParam String email,
                        @RequestParam String password) {

        UserDto loginMember = memberService.userLogin(email, password);

        if (loginMember != null) {
            httpSession.setAttribute("memberId", loginMember.getId());
            httpSession.setAttribute("email", loginMember.getEmail());
            httpSession.setAttribute("nickName", loginMember.getNickName());

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
        log.info("로그아웃 성공");
        return "redirect:/";
    }

    //로그인한 사용자의 세션에 저장된 이메일을 가져와서 프론트 화면에 보여주는 api
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
    public String signup(@RequestParam String email, @RequestParam String password, Model model) {
        boolean result = memberService.join(email, password);
        log.info("회원가입 로직 지나가요~~");

        if (result) {
            log.info("회원가입 성공");
            return "redirect:/";
        } else {
            log.info("회원가입 오류");
            model.addAttribute("errorMessage", "이미 사용 중인 이메일입니다.");
            return "signup";
        }

    }

    //회원정보 수정 페이지 이동
    @GetMapping("/edit")
    public String edit(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            return "redirect:/";
        }
        UserDto user = memberService.findUserByEmail(email);
        model.addAttribute("user", user);
        return "edit";
    }

    // 회원정보 수정 처리
    @PostMapping("/user/edit")
    public String updateUser(@ModelAttribute UserDto userDto,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        String oldEmail = (String) session.getAttribute("email");

        if (oldEmail == null) {
            return "redirect:/";
        }

        boolean result = memberService.updateUser(oldEmail, userDto);

        if (!result) {
            redirectAttributes.addFlashAttribute("message", "이미 사용 중인 이메일입니다.");
            return "redirect:/edit";
        }

        // 이메일이 변경되었으면 세션 이메일도 새 이메일로 변경
        session.setAttribute("email", userDto.getEmail());
        session.setAttribute("nickname", userDto.getNickName());

        redirectAttributes.addFlashAttribute("message", "회원정보가 수정되었습니다.");

        return "redirect:/home";
    }


    //회원정보 삭제
    @PostMapping("/user/delete")
    public String deleteUser(HttpSession session,
                             RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("email");

        if (email == null) {
            return "redirect:/";
        }
        memberService.deleteUser(email);
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "회원탈퇴가 완료되었습니다.");
        return "redirect:/";
    }


}
