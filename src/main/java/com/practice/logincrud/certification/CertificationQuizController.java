package com.practice.logincrud.certification;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 자격증 문제풀이(오답 가중치 출제) 전용 컨트롤러.
 * 기존 CertificationController(/certification, 내 자격증 CRUD)와는 별도로,
 * 요구된 URL 스펙(/certifications, memberId 쿼리 파라미터)을 그대로 따른다.
 *
 * 주의(보안 메모): memberId를 세션이 아니라 요청 파라미터로 받기 때문에,
 * 로그인은 LoginInterceptor가 걸러주지만 "다른 사람의 memberId를 URL에 넣어 조회/제출"하는 것까지는
 * 막지 못한다. 지금은 1인 개발/테스트 단계라 요구사항대로 두지만, 실사용자가 늘면
 * 세션의 memberId와 파라미터 memberId가 같은지 검증하는 로직을 추가해야 한다.
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class CertificationQuizController {

    private final CertificationService certificationService;
    private final CertificationQuestionService certificationQuestionService;

    // 자격증 목록 (문제풀이 진입점)
    @GetMapping("/certifications")
    public String list(@RequestParam Long memberId, Model model) {
        List<CertificationDto> certList = certificationService.getMyCertifications(memberId);

        model.addAttribute("certList", certList);
        model.addAttribute("memberId", memberId);
        return "certification/quiz-list";
    }

    // 문제 1개 출제 (가중 랜덤 + 직전 문제 연속 출제 방지)
    @GetMapping("/certifications/{certificationId}/quiz")
    public String quiz(@PathVariable Long certificationId,
                        @RequestParam Long memberId,
                        HttpSession session,
                        Model model) {

        String sessionKey = lastQuestionSessionKey(certificationId, memberId);
        Long lastQuestionId = (Long) session.getAttribute(sessionKey);

        CertificationQuestionDto question =
                certificationQuestionService.pickNextQuestion(certificationId, memberId, lastQuestionId);

        model.addAttribute("certificationId", certificationId);
        model.addAttribute("memberId", memberId);

        if (question == null) {
            log.info("문제 없음 certificationId={} memberId={}", certificationId, memberId);
            return "certification/quiz-empty";
        }

        session.setAttribute(sessionKey, question.getId());

        model.addAttribute("mode", "question");
        model.addAttribute("question", question);
        return "certification/quiz";
    }

    // 제출 + 즉시 채점
    @PostMapping("/certifications/{certificationId}/quiz/submit")
    public String submit(@PathVariable Long certificationId,
                         @RequestParam Long memberId,
                         @RequestParam Long questionId,
                         @RequestParam Integer selectedChoice,
                         Model model) {

        CertificationQuestionDto question = certificationQuestionService.findById(questionId);

        if (question == null) {
            log.warn("존재하지 않는 문제 제출 questionId={}", questionId);
            return "redirect:/certifications/" + certificationId + "/quiz?memberId=" + memberId;
        }

        boolean correct = question.getAnswerNo() != null && question.getAnswerNo().equals(selectedChoice);

        certificationQuestionService.recordAnswer(memberId, questionId, correct);

        model.addAttribute("certificationId", certificationId);
        model.addAttribute("memberId", memberId);
        model.addAttribute("mode", "result");
        model.addAttribute("question", question);
        model.addAttribute("correct", correct);
        model.addAttribute("selectedChoice", selectedChoice);
        return "certification/quiz";
    }

    private String lastQuestionSessionKey(Long certificationId, Long memberId) {
        return "lastQuestionId_" + certificationId + "_" + memberId;
    }
}
