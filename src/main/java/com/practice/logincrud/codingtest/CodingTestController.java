package com.practice.logincrud.codingtest;

import com.practice.logincrud.config.SessionConst;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * AI 코딩테스트 기능.
 * memberId는 항상 세션에서만 꺼낸다 (요청 파라미터로 받지 않음) — 다른 회원의 세션에 접근하는 것을 막기 위한
 * 최소한의 보안 조치. 세션 조회 시에도 소유권을 함께 검증한다 (interview 패키지와 동일한 패턴).
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class CodingTestController {

    private final CodingTestService codingTestService;

    @GetMapping("/codingtest")
    public String list(HttpSession session, Model model,
                        @RequestParam(required = false) String error) {
        Long memberId = (Long) session.getAttribute(SessionConst.Member_Id);

        model.addAttribute("sessionList", codingTestService.getMySessions(memberId));
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        return "codingtest/list";
    }

    @PostMapping("/codingtest/start")
    public String start(HttpSession session, @RequestParam String difficulty) {
        Long memberId = (Long) session.getAttribute(SessionConst.Member_Id);

        try {
            CodingTestSessionDto newSession = codingTestService.startSession(memberId, difficulty);
            return "redirect:/codingtest/sessions/" + newSession.getId();
        } catch (CodingTestException e) {
            log.warn("코딩테스트 시작 실패 memberId={} difficulty={} reason={}", memberId, difficulty, e.getMessage());
            return "redirect:/codingtest?error=" + encode(e.getMessage());
        }
    }

    @GetMapping("/codingtest/sessions/{sessionId}")
    public String detail(@PathVariable Long sessionId, HttpSession session, Model model) {
        Long memberId = (Long) session.getAttribute(SessionConst.Member_Id);

        CodingTestSessionDto testSession = codingTestService.getOwnedSession(sessionId, memberId);
        if (testSession == null) {
            return "redirect:/codingtest";
        }

        model.addAttribute("testSession", testSession);
        model.addAttribute("messageList", codingTestService.getMessages(sessionId));
        return "codingtest/detail";
    }

    // 답안 제출 (AJAX/fetch 전용, JSON 응답) — 언어 선택 + 코드 텍스트를 받아 AI 첨삭 결과를 반환한다.
    @PostMapping("/codingtest/sessions/{sessionId}/submit")
    @ResponseBody
    public ResponseEntity<?> submitAnswer(@PathVariable Long sessionId,
                                           HttpSession session,
                                           @RequestBody Map<String, String> body) {

        Long memberId = (Long) session.getAttribute(SessionConst.Member_Id);

        CodingTestSessionDto testSession = codingTestService.getOwnedSession(sessionId, memberId);
        if (testSession == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "접근 권한이 없습니다."));
        }

        try {
            CodingTestMessageDto feedback = codingTestService.submitAnswer(testSession, body.get("language"), body.get("code"));
            return ResponseEntity.ok(feedback);
        } catch (CodingTestException e) {
            log.warn("답안 제출 실패 sessionId={} memberId={} reason={}", sessionId, memberId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // 다국어 모범답안 요청 (AJAX/fetch 전용, JSON 응답)
    @PostMapping("/codingtest/sessions/{sessionId}/solutions")
    @ResponseBody
    public ResponseEntity<?> solutions(@PathVariable Long sessionId, HttpSession session) {
        Long memberId = (Long) session.getAttribute(SessionConst.Member_Id);

        CodingTestSessionDto testSession = codingTestService.getOwnedSession(sessionId, memberId);
        if (testSession == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "접근 권한이 없습니다."));
        }

        try {
            CodingTestMessageDto solutions = codingTestService.getOrGenerateSolutions(testSession);
            return ResponseEntity.ok(solutions);
        } catch (CodingTestException e) {
            log.warn("모범답안 생성 실패 sessionId={} memberId={} reason={}", sessionId, memberId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
