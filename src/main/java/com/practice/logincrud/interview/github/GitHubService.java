package com.practice.logincrud.interview.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.practice.logincrud.interview.InterviewIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GitHub 공개 저장소 정보를 조회하는 서비스.
 * 인증 없이 GitHub REST API를 호출하므로 시간당 60회 제한이 있다 (개인 포트폴리오 용도로는 충분).
 */
@Service
@Slf4j
public class GitHubService {

    private static final Pattern REPO_URL_PATTERN =
            Pattern.compile("github\\.com/([A-Za-z0-9_.-]+)/([A-Za-z0-9_.-]+?)(?:\\.git)?/?$");

    private static final int README_MAX_LENGTH = 4000;

    private final RestClient githubRestClient;

    public GitHubService(@Qualifier("githubRestClient") RestClient githubRestClient) {
        this.githubRestClient = githubRestClient;
    }

    /**
     * "https://github.com/owner/repo" 형태의 URL에서 owner/repo를 추출한다.
     * 형식이 맞지 않으면 예외를 던진다 (Validation 목적).
     */
    public String[] parseRepoUrl(String repoUrl) {
        if (repoUrl == null || repoUrl.isBlank()) {
            throw new InterviewIntegrationException("GitHub 저장소 URL을 입력해주세요.");
        }

        Matcher matcher = REPO_URL_PATTERN.matcher(repoUrl.trim());
        if (!matcher.find()) {
            throw new InterviewIntegrationException("올바른 GitHub 저장소 URL이 아닙니다. 예: https://github.com/owner/repo");
        }

        return new String[]{matcher.group(1), matcher.group(2)};
    }

    public GitHubRepoInfo fetchRepoInfo(String owner, String repo) {
        JsonNode repoJson = fetchRepoMeta(owner, repo);
        String readme = fetchReadme(owner, repo);

        String description = repoJson.path("description").isMissingNode() ? null : repoJson.path("description").asText(null);
        String language = repoJson.path("language").isMissingNode() ? null : repoJson.path("language").asText(null);

        return new GitHubRepoInfo(owner, repo, description, language, readme);
    }

    private JsonNode fetchRepoMeta(String owner, String repo) {
        try {
            JsonNode body = githubRestClient.get()
                    .uri("/repos/{owner}/{repo}", owner, repo)
                    .retrieve()
                    .body(JsonNode.class);

            if (body == null) {
                throw new InterviewIntegrationException("GitHub 저장소 정보를 가져오지 못했습니다.");
            }
            return body;

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("GitHub repo not found: {}/{}", owner, repo);
            throw new InterviewIntegrationException("저장소를 찾을 수 없습니다. URL을 다시 확인하거나 공개 저장소인지 확인해주세요.");
        } catch (HttpClientErrorException.Forbidden e) {
            log.warn("GitHub API rate limit exceeded while fetching {}/{}", owner, repo);
            throw new InterviewIntegrationException("GitHub API 요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
        } catch (HttpClientErrorException e) {
            log.error("GitHub API error {}/{} status={}", owner, repo, e.getStatusCode(), e);
            throw new InterviewIntegrationException("GitHub 저장소 정보를 가져오는 중 오류가 발생했습니다.");
        }
    }

    // README는 없을 수도 있는 정보이므로, 실패해도 전체 등록을 막지 않고 빈 문자열로 처리한다.
    private String fetchReadme(String owner, String repo) {
        try {
            JsonNode body = githubRestClient.get()
                    .uri("/repos/{owner}/{repo}/readme", owner, repo)
                    .retrieve()
                    .body(JsonNode.class);

            if (body == null) {
                return "";
            }

            String content = body.path("content").asText("");
            String encoding = body.path("encoding").asText("");

            if (!"base64".equals(encoding) || content.isBlank()) {
                return "";
            }

            String decoded = new String(
                    Base64.getMimeDecoder().decode(content),
                    StandardCharsets.UTF_8
            );

            return decoded.length() > README_MAX_LENGTH
                    ? decoded.substring(0, README_MAX_LENGTH) + "\n...(생략)"
                    : decoded;

        } catch (Exception e) {
            log.info("README 조회 실패(무시하고 진행) owner={} repo={} reason={}", owner, repo, e.getMessage());
            return "";
        }
    }
}
