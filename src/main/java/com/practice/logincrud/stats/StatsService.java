package com.practice.logincrud.stats;

import org.springframework.stereotype.Service;

/**
 * 홈 대시보드의 "실력 분석 & 취업 성공률" 계산 전담 서비스.
 * 계산식을 컨트롤러/화면에서 분리해두면 값이 바뀌어도 이 클래스만 고치면 되고, 단위 테스트도 붙이기 쉽다.
 *
 * 계산 방식(단순 평균):
 *   종합점수 = (자격증 합격률 + 면접 준비율 + 코딩테스트 준비율 + 출석률) / 4
 *   종합등급 = 종합점수를 A+ ~ D 구간으로 환산
 *   예상 취업 성공률 = 종합점수와 동일한 값을 그대로 노출
 *
 * 주의(정직하게 밝혀둘 부분): "예상 취업 성공률"은 실제 채용 데이터를 학습한 예측 모델이 아니라,
 * 지금까지의 준비 정도를 하나의 지표(백분율)로 환산해 보여주는 값이다. 코딩테스트는 아직 기능이
 * 없어 항상 0점으로 계산에 포함되며, 기능이 추가되면 이 부분만 실제 값으로 교체하면 된다.
 */
@Service
public class StatsService {

    public SkillStatsDto calculate(int certRate, int interviewRate, int codingTestRate, int attendanceRate) {
        int overallScore = (int) Math.round(
                (certRate + interviewRate + codingTestRate + attendanceRate) / 4.0);

        String grade = toGrade(overallScore);

        return new SkillStatsDto(certRate, interviewRate, codingTestRate, attendanceRate,
                overallScore, grade, overallScore);
    }

    private String toGrade(int score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B+";
        if (score >= 60) return "B";
        if (score >= 50) return "C+";
        if (score >= 40) return "C";
        return "D";
    }
}
