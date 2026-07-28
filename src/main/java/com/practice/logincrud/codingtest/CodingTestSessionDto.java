package com.practice.logincrud.codingtest;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CodingTestSessionDto {
    private Long id;
    private Long memberId;
    private String difficulty;     // 기초 / 초급 / 중급 / 고급
    private String problemText;
    private String status;         // IN_PROGRESS / ANSWERED
    private LocalDateTime createdAt;
}
