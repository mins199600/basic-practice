package com.practice.logincrud.codingtest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 코딩테스트 기능의 핵심 로직.
 * interview 패키지의 InterviewService와 동일한 구조 — memberId 소유권 검증을 서비스 계층에서 항상 함께 수행한다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CodingTestService {

    private final CodingTestSessionMapper codingTestSessionMapper;
    private final CodingTestMessageMapper codingTestMessageMapper;
    private final CodingTestAiService codingTestAiService;

    // 답안 제출(ANSWERED)이 이 개수만큼 쌓이면 "코딩테스트 준비율" 100%로 간주한다.
    private static final int PREP_TARGET_ANSWERED = 5;

    public List<CodingTestSessionDto> getMySessions(Long memberId) {
        return codingTestSessionMapper.findByMemberId(memberId);
    }

    /** 난이도를 받아 AI로 새 문제를 생성하고 세션을 시작한다. */
    public CodingTestSessionDto startSession(Long memberId, String difficulty) {
        String problemText = codingTestAiService.generateProblem(difficulty);

        CodingTestSessionDto session = new CodingTestSessionDto();
        session.setMemberId(memberId);
        session.setDifficulty(difficulty);
        session.setProblemText(problemText);
        codingTestSessionMapper.insert(session);

        saveMessage(session.getId(), "AI", "PROBLEM", null, problemText);

        log.info("코딩테스트 세션 시작 sessionId={} memberId={} difficulty={}", session.getId(), memberId, difficulty);
        return codingTestSessionMapper.findById(session.getId());
    }

    /** 소유권 검증까지 포함한 세션 단건 조회. 다른 회원의 세션이면 null을 반환한다. */
    public CodingTestSessionDto getOwnedSession(Long sessionId, Long memberId) {
        CodingTestSessionDto session = codingTestSessionMapper.findById(sessionId);
        if (session == null || !session.getMemberId().equals(memberId)) {
            return null;
        }
        return session;
    }

    public List<CodingTestMessageDto> getMessages(Long sessionId) {
        return codingTestMessageMapper.findBySessionId(sessionId);
    }

    /**
     * 지원자가 특정 언어로 작성한 답안을 제출하면, AI가 코드를 읽고 텍스트로 첨삭한다 (실행/채점 없음).
     * 재도전을 막지 않는다 — 다시 제출하면 이전 답안 이력 위에 새 답안/첨삭이 계속 쌓인다.
     */
    public CodingTestMessageDto submitAnswer(CodingTestSessionDto session, String language, String code) {
        if (code == null || code.isBlank()) {
            throw new CodingTestException("답안 코드를 입력해주세요.");
        }
        if (language == null || language.isBlank()) {
            throw new CodingTestException("언어를 선택해주세요.");
        }

        saveMessage(session.getId(), "USER", "ANSWER", language.toUpperCase(), code);

        String feedback = codingTestAiService.reviewAnswer(session.getProblemText(), language, code);
        CodingTestMessageDto feedbackMessage = saveMessage(session.getId(), "AI", "FEEDBACK", null, feedback);

        if (!"ANSWERED".equals(session.getStatus())) {
            codingTestSessionMapper.updateStatus(session.getId(), "ANSWERED");
        }

        return feedbackMessage;
    }

    /**
     * Java/Python/C 모범답안을 생성한다. 같은 세션에서 이미 생성한 적이 있으면 AI를 다시 호출하지 않고
     * 기존 메시지를 재사용한다 (불필요한 API 호출 방지).
     */
    public CodingTestMessageDto getOrGenerateSolutions(CodingTestSessionDto session) {
        List<CodingTestMessageDto> history = codingTestMessageMapper.findBySessionId(session.getId());
        for (CodingTestMessageDto m : history) {
            if ("SOLUTIONS".equals(m.getMessageType())) {
                return m;
            }
        }

        String solutions = codingTestAiService.generateSolutions(session.getProblemText());
        return saveMessage(session.getId(), "AI", "SOLUTIONS", null, solutions);
    }

    // 홈 대시보드용 - 코딩테스트 준비율(%). 답안 제출 완료(ANSWERED) 세션 개수를 기준으로 계산하며 100을 넘지 않는다.
    public int getPrepRate(Long memberId) {
        int answeredCount = codingTestSessionMapper.countAnsweredByMemberId(memberId);
        return Math.min(100, (int) Math.round(answeredCount * 100.0 / PREP_TARGET_ANSWERED));
    }

    private CodingTestMessageDto saveMessage(Long sessionId, String sender, String type, String language, String content) {
        CodingTestMessageDto dto = new CodingTestMessageDto();
        dto.setSessionId(sessionId);
        dto.setSender(sender);
        dto.setMessageType(type);
        dto.setLanguage(language);
        dto.setContent(content);
        codingTestMessageMapper.insert(dto);
        return dto;
    }
}
