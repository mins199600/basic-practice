package com.practice.logincrud.codingtest;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CodingTestMessageDto {
    private Long id;
    private Long sessionId;
    private String sender;         // AI / USER
    private String messageType;    // PROBLEM / ANSWER / FEEDBACK / SOLUTIONS
    private String language;       // ANSWER일 때만: JAVA / PYTHON / C (그 외엔 null)
    private String content;
    private LocalDateTime createdAt;
}
