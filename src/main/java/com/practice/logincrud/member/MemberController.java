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

    @Autowired
    EmailAuthService emailAuthService;

    @GetMapping("/")
    public String home(@RequestParam(required = false) String error,
                       @RequestParam(required = false) String passwordChanged,
                       Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "아이디 또는 비밀번호가 틀렸습니다.");
        }
        if (passwordChanged != null) {
            model.addAttribute("successMessage", "비밀번호가 변경되었습니다. 다시 로그인해주세요.");
        }
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
            httpSession.setAttribute("role", loginMember.getRole());
            httpSession.setAttribute("nickName", loginMember.getNickName());

            log.info("로그인 성공");
            return "redirect:/home";

        } else {
            log.info("로그인 실패");
            return "redirect:/?error=true";
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
        String nickName = (String) session.getAttribute("nickName");

        Map<String, String> response = new HashMap<>();
        response.put("email", email != null ? email : "로그인 안 함");
        response.put("nickName", nickName != null ? nickName : "");
        return response;
    }

    //회원가입 페이지 이동
    @GetMapping("/join")
    public String join() {
        return "signup";
    }

    //회원가입 로직
    @PostMapping("/signup")
    public String signup(@RequestParam String email,
                         @RequestParam String password,
                         @RequestParam(required = false) String nickName,
                         Model model,
                         HttpSession httpSession) {

        log.info("회원가입 로직 진입");
        log.info("회원가입 요청 email={}", email);

        // 1) 이메일 인증 여부 체크
        if (!emailAuthService.isVerified(email, httpSession)) {
            log.warn("회원가입 차단 - 이메일 미인증 email={}", email);
            model.addAttribute("errorMessage", "이메일 인증을 먼저 완료해주세요.");
            return "signup";
        }

        // 2) 회원가입
        boolean result = memberService.join(email, password, nickName);

        if (result) {
            // 3) 가입 끝나면 인증정보 삭제
            emailAuthService.clear(email, httpSession);
            log.info("회원가입 성공 email={}", email);
            return "redirect:/";
        } else {
            log.warn("회원가입 실패 - 중복 이메일 email={}", email);
            model.addAttribute("errorMessage", "이미 사용 중인 이메일입니다.");
            return "signup";
        }
    }

    //이메일 전송
    @PostMapping("/email/send-code")
    @ResponseBody
    public Map<String, Object> sendCode(@RequestParam String email, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        try {

            UserDto existing = memberService.findUserByEmail(email);
            if (existing != null) {
                log.warn("인증코드 발송 차단 - 이미 가입된 이메일 email={}", email);
                result.put("success", false);
                result.put("message", "이미 가입된 이메일입니다.");
                return result;
            }
        } catch (Exception e) {
            log.error("중복 이메일 조회 실패(DB 연결 문제) email={}", email, e);
            result.put("success", false);
            result.put("message", "서버 DB 연결 오류입니다. 잠시 후 다시 시도해주세요.");
            return result;
        }

        try {

            emailAuthService.sendCode(email, session);
            result.put("success", true);
            result.put("message", "인증번호를 발송했습니다.");
        } catch (Exception e) {
            log.error("메일 발송 실패 email={}", email, e);
            result.put("success", false);
            result.put("message", "메일 발송 실패: 메일 설정을 확인하세요.");
        }

        return result;
    }

    @PostMapping("/email/verify-code")
    @ResponseBody
    public Map<String, Object> verifyCode(@RequestParam String email,
                                          @RequestParam String code,
                                          HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        boolean ok = emailAuthService.verifyCode(email, code, session);
        result.put("success", ok);
        result.put("message", ok ? "이메일 인증 완료" : "인증번호가 올바르지 않거나 만료되었습니다.");
        return result;
    }

    //아이디 찾기 페이지 이동
    @GetMapping("/find-id")
    public String findIdForm() {
        return "member/find-id";
    }

    //아이디 찾기용 인증코드 발송 (닉네임으로 매칭되는 이메일에 발송, 이메일 자체는 노출하지 않음)
    @PostMapping("/find-id/send-code")
    @ResponseBody
    public Map<String, Object> sendFindIdCode(@RequestParam String nickname, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        String email;
        try {
            email = memberService.findEmailByNickname(nickname);
        } catch (Exception e) {
            log.error("아이디 찾기 이메일 조회 실패(DB 연결 문제) nickname={}", nickname, e);
            result.put("success", false);
            result.put("message", "서버 DB 연결 오류입니다. 잠시 후 다시 시도해주세요.");
            return result;
        }

        if (email == null) {
            log.warn("아이디 찾기 인증코드 발송 차단 - 일치하는 계정 없음 nickname={}", nickname);
            result.put("success", false);
            result.put("message", "일치하는 계정이 없습니다.");
            return result;
        }

        try {
            emailAuthService.sendCode(email, session);
            result.put("success", true);
            result.put("message", "가입된 이메일로 인증번호를 발송했습니다.");
        } catch (Exception e) {
            log.error("아이디 찾기 메일 발송 실패 nickname={}", nickname, e);
            result.put("success", false);
            result.put("message", "메일 발송 실패: 메일 설정을 확인하세요.");
        }

        return result;
    }

    //아이디 찾기용 인증코드 확인 - 인증 성공 시에만 이메일 공개
    @PostMapping("/find-id/verify-code")
    @ResponseBody
    public Map<String, Object> verifyFindIdCode(@RequestParam String nickname,
                                                @RequestParam String code,
                                                HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        String email = memberService.findEmailByNickname(nickname);
        if (email == null) {
            result.put("success", false);
            result.put("message", "일치하는 계정이 없습니다.");
            return result;
        }

        boolean ok = emailAuthService.verifyCode(email, code, session);
        result.put("success", ok);

        if (ok) {
            result.put("message", "이메일 인증 완료");
            result.put("email", email);
            emailAuthService.clear(email, session);
            log.info("아이디 찾기 성공 nickname={}", nickname);
        } else {
            result.put("message", "인증번호가 올바르지 않거나 만료되었습니다.");
        }

        return result;
    }

    //비밀번호 찾기 페이지 이동
    @GetMapping("/find-password")
    public String findPasswordForm() {
        return "member/find-password";
    }

    //비밀번호 재설정용 인증코드 발송
    @PostMapping("/password/send-code")
    @ResponseBody
    public Map<String, Object> sendPasswordResetCode(@RequestParam String email, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        UserDto existing;
        try {
            existing = memberService.findUserByEmail(email);
        } catch (Exception e) {
            log.error("가입 여부 조회 실패(DB 연결 문제) email={}", email, e);
            result.put("success", false);
            result.put("message", "서버 DB 연결 오류입니다. 잠시 후 다시 시도해주세요.");
            return result;
        }

        if (existing == null) {
            log.warn("비밀번호 재설정 인증코드 발송 차단 - 가입되지 않은 이메일 email={}", email);
            result.put("success", false);
            result.put("message", "가입된 이메일이 아닙니다.");
            return result;
        }

        try {
            emailAuthService.sendCode(email, session);
            result.put("success", true);
            result.put("message", "인증번호를 발송했습니다.");
        } catch (Exception e) {
            log.error("비밀번호 재설정 메일 발송 실패 email={}", email, e);
            result.put("success", false);
            result.put("message", "메일 발송 실패: 메일 설정을 확인하세요.");
        }

        return result;
    }

    //비밀번호 재설정용 인증코드 확인
    @PostMapping("/password/verify-code")
    @ResponseBody
    public Map<String, Object> verifyPasswordResetCode(@RequestParam String email,
                                                        @RequestParam String code,
                                                        HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        boolean ok = emailAuthService.verifyCode(email, code, session);
        result.put("success", ok);
        result.put("message", ok ? "이메일 인증 완료" : "인증번호가 올바르지 않거나 만료되었습니다.");
        return result;
    }

    //비밀번호 재설정
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String newPassword,
                                @RequestParam String newPasswordCheck,
                                HttpSession session,
                                Model model) {
        if (!emailAuthService.isVerified(email, session)) {
            log.warn("비밀번호 재설정 차단 - 이메일 미인증 email={}", email);
            model.addAttribute("error", "이메일 인증을 먼저 완료해주세요.");
            return "member/find-password";
        }

        if (!newPassword.equals(newPasswordCheck)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            return "member/find-password";
        }

        memberService.resetPassword(email, newPassword);
        emailAuthService.clear(email, session);
        log.info("비밀번호 재설정 성공 email={}", email);
        return "redirect:/?passwordChanged=true";
    }

    //회원정보 수정 페이지 이동
    @GetMapping("/edit")
    public String edit(HttpSession session, Model model) {
        String email = (String) session.getAttribute("email");
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

        boolean result = memberService.updateUser(oldEmail, userDto);

        if (!result) {
            redirectAttributes.addFlashAttribute("message", "이미 사용 중인 이메일입니다.");
            return "redirect:/edit";
        }

        // 이메일이 변경되었으면 세션 이메일도 새 이메일로 변경
        session.setAttribute("email", userDto.getEmail());
        session.setAttribute("nickName", userDto.getNickName());

        redirectAttributes.addFlashAttribute("message", "회원정보가 수정되었습니다.");

        return "redirect:/home";
    }


    //회원정보 삭제
    @PostMapping("/user/delete")
    public String deleteUser(HttpSession session,
                             RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("email");
        memberService.deleteUser(email);
        session.invalidate();
        redirectAttributes.addFlashAttribute("message", "회원탈퇴가 완료되었습니다.");
        return "redirect:/";
    }
}
