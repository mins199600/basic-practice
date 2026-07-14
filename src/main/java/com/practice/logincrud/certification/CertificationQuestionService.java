package com.practice.logincrud.certification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificationQuestionService {

    private final CertificationQuestionMapper certificationQuestionMapper;
    private final MemberQuestionStatMapper memberQuestionStatMapper;

    /**
     * 출제 정책:
     *  - 오답 문제일수록 weight가 높아서 더 자주 뽑힌다 (가중치 랜덤).
     *  - 직전에 낸 문제(excludeQuestionId)는 후보가 2개 이상이면 이번 회차에서 제외한다.
     *    후보가 1개뿐이면 예외 없이 그 문제를 다시 낸다.
     *  - 문제가 하나도 없으면 null을 반환한다 (호출부에서 "문제 없음" 화면 처리).
     */
    public CertificationQuestionDto pickNextQuestion(Long certificationId, Long memberId, Long excludeQuestionId) {
        List<CertificationQuestionDto> candidates =
                certificationQuestionMapper.findCandidatesWithWeight(certificationId, memberId);

        if (candidates.isEmpty()) {
            log.info("출제 가능한 문제 없음 certificationId={}", certificationId);
            return null;
        }

        List<CertificationQuestionDto> pool = candidates;

        if (candidates.size() > 1 && excludeQuestionId != null) {
            List<CertificationQuestionDto> filtered = candidates.stream()
                    .filter(q -> !q.getId().equals(excludeQuestionId))
                    .collect(Collectors.toList());
            if (!filtered.isEmpty()) {
                pool = filtered;
            }
        }

        return pickWeightedRandom(pool);
    }

    // 가중치 합계 범위 안에서 난수를 뽑아 누적합으로 문제를 선택 (weight가 클수록 뽑힐 확률이 높다)
    private CertificationQuestionDto pickWeightedRandom(List<CertificationQuestionDto> pool) {
        int totalWeight = pool.stream()
                .mapToInt(q -> q.getWeight() != null ? q.getWeight() : 1)
                .sum();

        if (totalWeight <= 0) {
            // 방어 코드: 이론상 weight는 항상 1 이상이라 여기 오면 안 되지만, 혹시 몰라 균등 선택으로 폴백
            return pool.get(ThreadLocalRandom.current().nextInt(pool.size()));
        }

        int r = ThreadLocalRandom.current().nextInt(totalWeight) + 1; // 1 ~ totalWeight
        int cumulative = 0;

        for (CertificationQuestionDto q : pool) {
            cumulative += (q.getWeight() != null ? q.getWeight() : 1);
            if (r <= cumulative) {
                return q;
            }
        }

        return pool.get(pool.size() - 1); // 이론상 도달하지 않음
    }

    public CertificationQuestionDto findById(Long questionId) {
        return certificationQuestionMapper.findById(questionId);
    }

    public boolean hasQuestions(Long certificationId) {
        return certificationQuestionMapper.countByCertificationId(certificationId) > 0;
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
