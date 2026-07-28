package com.practice.logincrud.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 홈 대시보드 "오늘의 학습 요약" 카드에 표시할 값 모음.
 * 자격증 문제풀이(member_question_stat)와 코딩테스트(coding_test_session) 활동을 합산한다.
 */
@Data
@AllArgsConstructor
public class StudySummaryDto {
    private int todayCount;              // 오늘 학습한 문제/코딩테스트 합계
    private int weekCount;                // 이번 주(월요일부터) 학습한 문제/코딩테스트 합계
    private String weakestSubjectName;    // 정답률이 가장 낮은 과목명 (표본 부족/미학습 시 null)
    private Integer weakestSubjectAccuracy; // 위 과목의 정답률(%) (weakestSubjectName이 null이면 null)
}
