package com.practice.logincrud.certification;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CertificationQuestionDto {
    private Long id;
    private Long certificationId;
    private String questionText;
    private String choice1;
    private String choice2;
    private String choice3;
    private String choice4;
    private Integer answerNo;      // 정답 번호 1~4
    private String explanation;    // 오답일 때 보여줄 해설
    private LocalDateTime createdAt;

    // 출제 후보 조회(member_question_stat 조인) 시에만 채워지는 필드.
    // 문제 자체의 컬럼이 아니라 "이 회원 기준 현재 가중치"를 담는 용도.
    private Integer weight;
}
