package com.practice.logincrud.stats;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 홈 대시보드 "실력 분석 & 취업 성공률" 카드에 표시할 값 모음.
 */
@Data
@AllArgsConstructor
public class SkillStatsDto {
    private int certRate;         // 자격증 합격률(%)
    private int interviewRate;    // 면접 준비율(%)
    private int codingTestRate;   // 코딩테스트 준비율(%) - 답안 제출 완료 세션 수 기준
    private int attendanceRate;   // 출석률(%)
    private int overallScore;     // 위 4개 항목의 평균(0~100)
    private String grade;         // overallScore를 등급(A+~D)으로 환산한 값
    private int successRate;      // 화면에 "예상 취업 성공률"로 노출 — overallScore와 동일한 값
}
