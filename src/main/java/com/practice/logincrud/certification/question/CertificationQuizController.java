package com.practice.logincrud.certification.question;

import com.practice.logincrud.certification.CertificationDto;
import com.practice.logincrud.certification.CertificationService;
import com.practice.logincrud.certification.subject.SubjectDto;
import com.practice.logincrud.certification.subject.SubjectService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/*
 * subjectId 파라미터 정책:
 *  - 과목이 등록된 자격증(예: 피부자격증)은 /subjects 화면을 거쳐 subjectId를 받아온다.
 *  - subjectId가 없는 자격증(과목 미분류)은 기존처럼 자격증 전체 문제를 대상으로 출제한다(하위 호환).
 *
 * 출제 방식 - "전체 → 오답만 → 또 오답만 → 100점":
 *  - 1회차는 (카탈로그, 과목) 범위의 전체 문제를 id 순서대로 처음부터 끝까지 낸다.
 *  - 한 회차가 끝났을 때 이번 회차에서 하나라도 틀렸다면, 틀린 문제만 모아 다음 회차를 시작한다.
 *  - 틀린 문제가 하나도 없을 때까지(=100점) 이 과정을 반복한다.
 *  - 진행 상태(QuizRoundState)는 DB가 아니라 세션에 저장한다 - 로그아웃/세션 만료 시 처음부터 다시 시작된다.
 *
 * catalogId 정책(2026-07-28 리팩터링):
 *  - 예전엔 이 경로변수가 회원 개인의 certification.id였다 - 회원마다 값이 달라서,
 *    문제은행이 고정된 하나의 id에만 연결돼 있으면 다른 회원은 문제를 못 보는 버그가 있었다.
 *  - 지금은 certification_catalog.id(자격증 종류 마스터, 회원과 무관하게 고정)를 가리킨다.
 *    회원의 자격증 목록(certList)에서 각 항목의 catalogId를 링크에 사용해야 한다.
 */
/**
 * 자격증 문제풀이(전체 → 오답만 반복 출제) 전용 컨트롤러.
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
    @GetMapping("/certifications/{catalogId}/subjects")
    public String subjects(@PathVariable Long catalogId,
                            @RequestParam Long memberId,
                            Model model) {

        List<SubjectDto> subjectList = subjectService.getSubjects(catalogId);

        if (subjectList.isEmpty()) {
            return "redirect:/certifications/" + catalogId + "/quiz?memberId=" + memberId;
        }

        model.addAttribute("certificationId", catalogId);
        model.addAttribute("memberId", memberId);
        model.addAttribute("subjectList", subjectList);
        return "certification/subject-list";
    }

    // 문제 1개 출제 - 현재 회차 큐의 맨 앞 문제를 꺼내 보여준다. 큐가 비었으면 다음 회차로 넘어가거나 완료 처리한다.
    @GetMapping("/certifications/{catalogId}/quiz")
    public String quiz(@PathVariable Long catalogId,
                        @RequestParam Long memberId,
                        @RequestParam(required = false) Long subjectId,
                        HttpSession session,
                        Model model) {

        model.addAttribute("certificationId", catalogId);
        model.addAttribute("memberId", memberId);
        model.addAttribute("subjectId", subjectId);

        String sessionKey = quizRoundSessionKey(catalogId, subjectId);
        QuizRoundState state = (QuizRoundState) session.getAttribute(sessionKey);

        // 진행 중인 회차가 없으면(처음 시작, 또는 세션 만료) 전체 문제로 1회차를 새로 연다.
        if (state == null) {
            List<Long> allIds = certificationQuestionService.getQuestionIdsInOrder(catalogId, subjectId);
            if (allIds.isEmpty()) {
                log.info("문제 없음 catalogId={} subjectId={} memberId={}", catalogId, subjectId, memberId);
                return "certification/quiz-empty";
            }
            state = new QuizRoundState(1, new ArrayList<>(allIds), new ArrayList<>(), allIds.size());
            session.setAttribute(sessionKey, state);
        }

        // 이번 회차 문제를 다 풀었으면: 오답이 없으면 완료(100점), 있으면 오답만 모아 다음 회차 시작
        if (state.getRemainingQueue().isEmpty()) {
            if (state.getWrongThisRound().isEmpty()) {
                session.removeAttribute(sessionKey);
                log.info("과목 100점 완료 catalogId={} subjectId={} memberId={}", catalogId, subjectId, memberId);
                return "certification/quiz-complete";
            }
            List<Long> nextQueue = new ArrayList<>(state.getWrongThisRound());
            state = new QuizRoundState(state.getRoundNumber() + 1, nextQueue, new ArrayList<>(), nextQueue.size());
            session.setAttribute(sessionKey, state);
        }

        Long nextQuestionId = state.getRemainingQueue().get(0);
        CertificationQuestionDto question = certificationQuestionService.findById(nextQuestionId);

        if (question == null) {
            // 방어 코드: 큐에 있던 문제가 그 사이 삭제된 경우 - 건너뛰고 다음 문제로
            state.getRemainingQueue().remove(0);
            return "redirect:/certifications/" + catalogId + "/quiz?memberId=" + memberId
                    + (subjectId != null ? "&subjectId=" + subjectId : "");
        }

        int solvedInRound = state.getTotalInRound() - state.getRemainingQueue().size();

        model.addAttribute("mode", "question");
        model.addAttribute("question", question);
        model.addAttribute("roundNumber", state.getRoundNumber());
        model.addAttribute("progressCurrent", solvedInRound + 1);
        model.addAttribute("progressTotal", state.getTotalInRound());
        return "certification/quiz";
    }

    // 제출 + 즉시 채점. 큐 맨 앞과 제출된 문제가 일치할 때만 큐를 전진시킨다(새로고침/중복 제출 방지).
    @PostMapping("/certifications/{catalogId}/quiz/submit")
    public String submit(@PathVariable Long catalogId,
                         @RequestParam Long memberId,
                         @RequestParam(required = false) Long subjectId,
                         @RequestParam Long questionId,
                         @RequestParam Integer selectedChoice,
                         HttpSession session,
                         Model model) {

        CertificationQuestionDto question = certificationQuestionService.findById(questionId);

        if (question == null) {
            log.warn("존재하지 않는 문제 제출 questionId={}", questionId);
            return "redirect:/certifications/" + catalogId + "/quiz?memberId=" + memberId
                    + (subjectId != null ? "&subjectId=" + subjectId : "");
        }

        boolean correct = question.getAnswerNo() != null && question.getAnswerNo().equals(selectedChoice);
        certificationQuestionService.recordAnswer(memberId, questionId, correct);

        String sessionKey = quizRoundSessionKey(catalogId, subjectId);
        QuizRoundState state = (QuizRoundState) session.getAttribute(sessionKey);

        if (state != null && !state.getRemainingQueue().isEmpty()
                && state.getRemainingQueue().get(0).equals(questionId)) {
            state.getRemainingQueue().remove(0);
            if (!correct) {
                state.getWrongThisRound().add(questionId);
            }
        }

        model.addAttribute("certificationId", catalogId);
        model.addAttribute("memberId", memberId);
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("mode", "result");
        model.addAttribute("question", question);
        model.addAttribute("correct", correct);
        model.addAttribute("selectedChoice", selectedChoice);

        if (state != null) {
            int solvedInRound = state.getTotalInRound() - state.getRemainingQueue().size();
            model.addAttribute("roundNumber", state.getRoundNumber());
            model.addAttribute("progressCurrent", solvedInRound);
            model.addAttribute("progressTotal", state.getTotalInRound());
        }

        return "certification/quiz";
    }

    private String quizRoundSessionKey(Long catalogId, Long subjectId) {
        return "quizRound_" + catalogId + "_" + subjectId;
    }
}
