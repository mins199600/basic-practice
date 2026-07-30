package com.practice.logincrud.certification;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CertificationQuestionDto {
    private Long id;
    private Long catalogId;
    private Long subjectId;
    private String questionText;
    private String choice1;
    private String choice2;
    private String choice3;
    private String choice4;
    private Integer answerNo;      // 정답 번호 1~4
    private String explanation;    // 오답일 때 보여줄 해설
    private LocalDateTime createdAt;

    // 관리자 목록 화면에서 과목명을 같이 보여주기 위한 조인 전용 필드 (DB 컬럼 아님)
    private String subjectName;
}
