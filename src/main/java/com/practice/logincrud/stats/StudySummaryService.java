package com.practice.logincrud.stats;

import com.practice.logincrud.certification.MemberQuestionStatMapper;
import com.practice.logincrud.certification.WeakestSubjectDto;
import com.practice.logincrud.codingtest.CodingTestSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 홈 대시보드 "오늘의 학습 요약" 계산 전담 서비스.
 * 자격증 문제풀이 통계(member_question_stat)와 코딩테스트 세션(coding_test_session)을 합산해서 보여준다.
 */
@Service
@RequiredArgsConstructor
public class StudySummaryService {

    private final MemberQuestionStatMapper memberQuestionStatMapper;
    private final CodingTestSessionMapper codingTestSessionMapper;

    public StudySummaryDto getSummary(Long memberId) {
        int todayCount = memberQuestionStatMapper.countTodayByMemberId(memberId)
                + codingTestSessionMapper.countTodayByMemberId(memberId);
        int weekCount = memberQuestionStatMapper.countThisWeekByMemberId(memberId)
                + codingTestSessionMapper.countThisWeekByMemberId(memberId);

        WeakestSubjectDto weakest = memberQuestionStatMapper.findWeakestSubject(memberId);
        String weakestSubjectName = weakest != null ? weakest.getSubjectName() : null;
        Integer weakestSubjectAccuracy = weakest != null ? weakest.getAccuracy() : null;

        return new StudySummaryDto(todayCount, weekCount, weakestSubjectName, weakestSubjectAccuracy);
    }
}
