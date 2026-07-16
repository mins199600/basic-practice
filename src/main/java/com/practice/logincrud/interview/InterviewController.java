package com.practice.logincrud.interview;

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
 * 프로젝트 면접 준비 기능.
 * memberId는 항상 세션에서만 꺼낸다 (요청 파라미터로 받지 않음) — 다른 회원의 프로젝트/세션에
 * 접근하는 것을 막기 위한 최소한의 보안 조치. 프로젝트/세션 조회 시에도 소유권을 함께 검증한다.
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping("/interview")
    public String list(HttpSession session, Model model,
                        @RequestParam(required = false) String error) {
        Long memberId = (Long) session.getAttribute(SessionConst.Member_Id);

        model.addAttribute("projectList", interviewService.getMyProjects(memberId));
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        return "interview/list";
    }

    @PostMapping("/interview/register")
    public String register(HttpSession session, @RequestParam String repoUrl) {
        Long memberId = (Long) session.getAttribute(SessionConst.Member_Id);

        try {
            InterviewProjectDto project = interviewService.registerProject(memberId, repoUrl);
            return "redirect:/interview/" + project.getId();
        } catch (InterviewIntegrationException e) {
            log.warn("프로젝트 등록 실패 memberId={} repoUrl={} reason={}", memberId, repoUrl, e.getMessage());
            return "redirect:/interview?error=" + encode(e.getMessage());
        }
    }

    @GetMapping("/interview/{projectId}")
    public String detail(@PathVariable Long projectId, HttpSession session, Model model,
                          @RequestParam(required = false) String error) {
        Long memberId = (Long) session.getAttribute(SessionConst.Member_Id);

        InterviewProjectDto project = interviewService.getOwnedProject(projectId, memberId);
        if (project == null) {
            return "redirect:/interview";
        }

        model.addAttribute("project", project);
        model.addAttribute("sessionList", interviewService.getSessions(projectId));
        if (error != null) {
            model.addAttribute("errorMessage", error);
        }
        return "interview/detail";
    }

    @PostMapping("/interview/{projectId}/sessions")
    public String startSession(@PathVariable Long projectId, HttpSession session) {
        Long memberId = (Long) session.getAttribute(SessionConst.Member_Id);

        try {
            InterviewSessionDto newSession = interviewService.startSession(projectId, memberId);
            return "redirect:/interview/sessions/" + newSession.getId();
        } catch (InterviewIntegrationException e) {
            log.warn("면접 세션 시작 실패 projectId={} memberId={} reason={}", projectId, memberId, e.getMessage());
            return "redirect:/interview/" + projectId + "?error=" + encode(e.getMessage());
        }
    }

    @GetMapping("/interview/sessions/{sessionId}")
    public String chat(@PathVariable Long sessionId, HttpSession session, Model model) {
        Long memberId = (Long) session.getAttribute(SessionConst.Member_Id);

        InterviewSessionDto interviewSession = interviewService.getOwnedSession(sessionId, memberId);
        if (interviewSession == null) {
            return "redirect:/interview";
        }

        model.addAttribute("interviewSession", interviewSession);
        model.addAttribute("messageList", interviewService.getMessages(sessionId));
        return "interview/chat";
    }

    // 답변 제출 (AJAX/fetch 전용, JSON 응답)
    @PostMapping("/interview/sessions/{sessionId}/messages")
    @ResponseBody
    public ResponseEntity<?> submitAnswer(@PathVariable Long sessionId,
                                           HttpSession session,
                                           @RequestBody Map<String, String> body) {
        Long memberId = (Long) session.getAttribute(SessionConst.Member_Id);

        InterviewSessionDto interviewSession = interviewService.getOwnedSession(sessionId, memberId);
        if (interviewSession == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "접근 권한이 없습니다."));
        }

        try {
            InterviewMessageDto aiMessage = interviewService.submitAnswer(interviewSession, body.get("content"));
            return ResponseEntity.ok(aiMessage);
        } catch (InterviewIntegrationException e) {
            log.warn("답변 제출 실패 sessionId={} memberId={} reason={}", sessionId, memberId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/interview/sessions/{sessionId}/end")
    public String endSession(@PathVariable Long sessionId, HttpSession session) {
        Long memberId = (Long) session.getAttribute(SessionConst.Member_Id);

        InterviewSessionDto interviewSession = interviewService.getOwnedSession(sessionId, memberId);
        if (interviewSession == null) {
            return "redirect:/interview";
        }

        interviewService.endSession(sessionId);
        return "redirect:/interview/" + interviewSession.getProjectId();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
