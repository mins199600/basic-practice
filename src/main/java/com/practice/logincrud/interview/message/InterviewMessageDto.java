package com.practice.logincrud.interview.message;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewMessageDto {
    private Long id;
    private Long sessionId;
    private String sender;         // AI / USER
    private String messageType;    // QUESTION / ANSWER / FEEDBACK
    private String content;
    private LocalDateTime createdAt;
}
