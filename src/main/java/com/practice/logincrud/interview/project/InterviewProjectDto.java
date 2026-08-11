package com.practice.logincrud.interview.project;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewProjectDto {
    private Long id;
    private Long memberId;
    private String repoUrl;
    private String repoOwner;
    private String repoName;
    private String description;
    private String primaryLanguage;
    private String readmeText;
    private LocalDateTime createdAt;
}
