package com.practice.logincrud.interview;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InterviewSessionDto {
    private Long id;
    private Long projectId;
    private Long memberId;
    private String status;          // IN_PROGRESS / ENDED
    private Integer questionCount;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;

    // 목록 화면에서 프로젝트명을 같이 보여주기 위한 조인 전용 필드 (DB 컬럼 아님)
    private String repoName;
}
