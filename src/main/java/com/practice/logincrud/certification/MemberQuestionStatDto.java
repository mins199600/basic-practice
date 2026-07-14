package com.practice.logincrud.certification;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MemberQuestionStatDto {
    private Long id;
    private Long memberId;
    private Long questionId;
    private Integer weight;
    private Integer correctCount;
    private Integer wrongCount;
    private LocalDateTime lastAnsweredAt;
}
