package com.practice.logincrud.interview.github;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * GitHub API 조회 결과를 담는 값 객체. DB 테이블과 무관한 순수 조회용 DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitHubRepoInfo {
    private String owner;
    private String name;
    private String description;
    private String primaryLanguage;
    private String readmeText;   // AI 프롬프트에 넣을 README 본문 (길이 제한 적용됨)
}
