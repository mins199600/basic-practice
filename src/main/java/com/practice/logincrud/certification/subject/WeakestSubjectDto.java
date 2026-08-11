package com.practice.logincrud.certification.subject;

import lombok.Data;

/**
 * 회원의 과목별 정답률 중 가장 낮은 과목 1개를 담는 조회 전용 DTO (member_question_stat + subject 조인 결과).
 */
@Data
public class WeakestSubjectDto {
    private String subjectName;
    private Integer accuracy; // 정답률(%)
}
