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

/*
 * subjectId 파라미터 정책:
 *  - 과목이 등록된 자격증(예: 피부자격증)은 /subjects 화면을 거쳐 subjectId를 받아온다.
 *  - subjectId가 없는 자격증(과목 미분류)은 기존처럼 자격증 전체 문제를 대상으로 출제한다(하위 호환).
 *  - "직전 문제 연속 출제 방지"는 과목 단위로 독립적으로 동작해야 하므로 세션 키에 subjectId도 포함한다.
 */

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
    private final SubjectService subjectService;

    // 자격증 목록 (문제풀이 진입점)
    @GetMapping("/certifications")
    public String list(@RequestParam Long memberId, Model model) {
        List<CertificationDto> certList = certificationService.getMyCertifications(memberId);

        model.addAttribute("certList", certList);
        model.addAttribute("memberId", memberId);
        return "certification/quiz-list";
    }

    // 과목(파트) 선택 화면. 과목이 등록되어 있지 않은 자격증이면 바로 전체 문제풀이로 넘긴다.
    @GetMapping("/certifications/{certificationId}/subjects")
    public String subjects(@PathVariable Long certificationId,
                            @RequestParam Long memberId,
                            Model model) {

        List<SubjectDto> subjectList = subjectService.getSubjects(certificationId);

        if (subjectList.isEmpty()) {
            return "redirect:/certifications/" + certificationId + "/quiz?memberId=" + memberId;
        }

        model.addAttribute("certificationId", certificationId);
        model.addAttribute("memberId", memberId);
        model.addAttribute("subjectList", subjectList);
        return "certification/subject-list";
    }

    // 문제 1개 출제 (가중 랜덤 + 직전 문제 연속 출제 방지, 과목 단위)
    @GetMapping("/certifications/{certificationId}/quiz")
    public String quiz(@PathVariable Long certificationId,
                        @RequestParam Long memberId,
                        @RequestParam(required = false) Long subjectId,
                        HttpSession session,
                        Model model) {

        String sessionKey = lastQuestionSessionKey(certificationId, subjectId, memberId);
        Long lastQuestionId = (Long) session.getAttribute(sessionKey);

        CertificationQuestionDto question =
                certificationQuestionService.pickNextQuestion(certificationId, subjectId, memberId, lastQuestionId);

        model.addAttribute("certificationId", certificationId);
        model.addAttribute("memberId", memberId);
        model.addAttribute("subjectId", subjectId);

        if (question == null) {
            log.info("문제 없음 certificationId={} subjectId={} memberId={}", certificationId, subjectId, memberId);
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
                         @RequestParam(required = false) Long subjectId,
                         @RequestParam Long questionId,
                         @RequestParam Integer selectedChoice,
                         Model model) {

        CertificationQuestionDto question = certificationQuestionService.findById(questionId);

        if (question == null) {
            log.warn("존재하지 않는 문제 제출 questionId={}", questionId);
            return "redirect:/certifications/" + certificationId + "/quiz?memberId=" + memberId
                    + (subjectId != null ? "&subjectId=" + subjectId : "");
        }

        boolean correct = question.getAnswerNo() != null && question.getAnswerNo().equals(selectedChoice);

        certificationQuestionService.recordAnswer(memberId, questionId, correct);

        model.addAttribute("certificationId", certificationId);
        model.addAttribute("memberId", memberId);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("mode", "result");
        model.addAttribute("question", question);
        model.addAttribute("correct", correct);
        model.addAttribute("selectedChoice", selectedChoice);
        return "certification/quiz";
    }

    private String lastQuestionSessionKey(Long certificationId, Long subjectId, Long memberId) {
        return "lastQuestionId_" + certificationId + "_" + subjectId + "_" + memberId;
    }
}
