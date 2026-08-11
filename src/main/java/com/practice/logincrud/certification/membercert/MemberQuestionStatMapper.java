package com.practice.logincrud.certification.membercert;

import com.practice.logincrud.certification.subject.WeakestSubjectDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberQuestionStatMapper {

    // 정오답 결과를 반영해 가중치/정오답 횟수를 upsert 한다.
    // weightDelta: 정답 -1 / 오답 +2 (최소값 1로 clamp는 SQL의 GREATEST()에서 처리)
    // correctInc / wrongInc: 이번 제출 결과에 따라 둘 중 하나만 1, 나머지는 0
    void upsertStat(@Param("memberId") Long memberId,
                     @Param("questionId") Long questionId,
                     @Param("weightDelta") int weightDelta,
                     @Param("correctInc") int correctInc,
                     @Param("wrongInc") int wrongInc);

    // 홈 "오늘의 학습 요약"용 - 오늘/이번 주(월요일 시작) 안에 답을 제출한 서로 다른 문제 개수
    int countTodayByMemberId(@Param("memberId") Long memberId);

    int countThisWeekByMemberId(@Param("memberId") Long memberId);

    // 과목별 정답률이 가장 낮은 과목 1개 (최소 3문제 이상 풀어본 과목만 대상 - 표본이 너무 적으면 제외)
    WeakestSubjectDto findWeakestSubject(@Param("memberId") Long memberId);
}
