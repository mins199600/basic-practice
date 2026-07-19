package com.practice.logincrud.certification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificationQuestionService {

    private final CertificationQuestionMapper certificationQuestionMapper;
    private final MemberQuestionStatMapper memberQuestionStatMapper;

    /**
     * "전체 → 오답만 → 또 오답만 → 100점" 방식 문제풀이의 1회차 큐를 구성할 때 쓰는 id 목록(id 오름차순).
     * subjectId가 null이면 자격증 전체 문제 대상 (과목 분류가 없는 자격증과의 하위 호환).
     */
    public List<Long> getQuestionIdsInOrder(Long certificationId, Long subjectId) {
        return certificationQuestionMapper.findQuestionIdsInOrder(certificationId, subjectId);
    }

    public CertificationQuestionDto findById(Long questionId) {
        return certificationQuestionMapper.findById(questionId);
    }

    public boolean hasQuestions(Long certificationId, Long subjectId) {
        return certificationQuestionMapper.countByCertificationId(certificationId, subjectId) > 0;
    }

    /**
     * 채점 결과를 회원-문제 통계에 반영한다.
     * 정답: weight -1 (최소 1) / 오답: weight +2
     */
    public void recordAnswer(Long memberId, Long questionId, boolean correct) {
        int weightDelta = correct ? -1 : 2;
        int correctInc = correct ? 1 : 0;
        int wrongInc = correct ? 0 : 1;

        memberQuestionStatMapper.upsertStat(memberId, questionId, weightDelta, correctInc, wrongInc);

        log.info("문제 통계 갱신 memberId={} questionId={} correct={} weightDelta={}",
                memberId, questionId, correct, weightDelta);
    }
}
