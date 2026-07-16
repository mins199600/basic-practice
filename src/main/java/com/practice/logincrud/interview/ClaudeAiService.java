package com.practice.logincrud.interview;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Anthropic Claude API를 호출해 기술 면접 질문/첨삭을 생성하는 서비스.
 * 대화 맥락(messages)은 항상 "면접 시작" 안내(user)로 시작해서 이후 AI 질문(assistant)/지원자 답변(user)이
 * 번갈아 이어지는 형태로 구성한다. Anthropic Messages API는 첫 메시지가 반드시 user role이어야 하기 때문이다.
 */
@Service
@Slf4j
public class ClaudeAiService {

    private static final String KICKOFF_MESSAGE = "면접을 시작합니다. 프로젝트를 기반으로 첫 번째 기술 질문을 해주세요.";

    private final RestClient anthropicRestClient;
    private final String model;

    public ClaudeAiService(@Qualifier("anthropicRestClient") RestClient anthropicRestClient,
                            @Value("${anthropic.model:claude-sonnet-4-5}") String model) {
        this.anthropicRestClient = anthropicRestClient;
        this.model = model;
    }

    /** 세션 시작 시 첫 질문을 생성한다. */
    public String startInterview(GitHubRepoInfo repoInfo) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", KICKOFF_MESSAGE));
        return callClaude(buildSystemPrompt(repoInfo), messages);
    }

    /** 지원자가 답변을 제출한 이후, 첨삭 + 다음 질문을 생성한다. */
    public String continueInterview(GitHubRepoInfo repoInfo, List<InterviewMessageDto> history) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", KICKOFF_MESSAGE));

        for (InterviewMessageDto m : history) {
            String role = "AI".equals(m.getSender()) ? "assistant" : "user";
            messages.add(Map.of("role", role, "content", m.getContent()));
        }

        return callClaude(buildSystemPrompt(repoInfo), messages);
    }

    private String buildSystemPrompt(GitHubRepoInfo repoInfo) {
        return """
                너는 신입 개발자 채용 기술 면접관이다. 아래 지원자의 GitHub 프로젝트를 기반으로 실전처럼 기술 면접을 진행한다.

                [프로젝트 정보]
                - 저장소: %s/%s
                - 설명: %s
                - 주요 언어: %s
                - README 발췌:
                %s

                [진행 규칙]
                1. 질문은 항상 한 번에 하나만 한다.
                2. Java/Spring Boot, DB 설계, 트러블슈팅, 프로젝트 설계 의도 등 실무 기술 질문 위주로 한다.
                3. 지원자가 답변을 제출하면 반드시 다음 순서로 응답한다:
                   (1) 첨삭 — 잘한 점 1~2개, 아쉬운 점 1~2개, 모범 답안 방향을 간결히 제시
                   (2) 이어서 다음 질문 1개
                4. 대화의 첫 메시지("면접을 시작합니다...")에는 첨삭 없이 질문 1개만 한다.
                5. 존댓말을 사용하고, 첨삭+질문을 합쳐 400자 내외로 간결하게 작성한다.
                6. 굵게(**) 정도만 사용하고 과도한 마크다운 서식은 쓰지 않는다.
                """.formatted(
                repoInfo.getOwner(),
                repoInfo.getName(),
                blankToDash(repoInfo.getDescription()),
                blankToDash(repoInfo.getPrimaryLanguage()),
                blankToDash(repoInfo.getReadmeText())
        );
    }

    private String blankToDash(String value) {
        return (value == null || value.isBlank()) ? "(정보 없음)" : value;
    }

    private String callClaude(String systemPrompt, List<Map<String, String>> messages) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", 1024);
        requestBody.put("system", systemPrompt);
        requestBody.put("messages", messages);

        try {
            JsonNode response = anthropicRestClient.post()
                    .uri("/v1/messages")
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.has("content") || response.path("content").isEmpty()) {
                throw new InterviewIntegrationException("AI 응답을 받지 못했습니다. 잠시 후 다시 시도해주세요.");
            }

            return response.path("content").get(0).path("text").asText("").trim();

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Anthropic API 인증 실패 — ANTHROPIC_API_KEY 확인 필요", e);
            throw new InterviewIntegrationException("AI 서비스 인증에 실패했습니다. 관리자에게 문의해주세요.");
        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("Anthropic API rate limit 초과");
            throw new InterviewIntegrationException("AI 요청이 많아 잠시 지연되고 있습니다. 잠시 후 다시 시도해주세요.");
        } catch (HttpClientErrorException e) {
            log.error("Anthropic API 오류 status={} body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new InterviewIntegrationException("AI 응답 생성 중 오류가 발생했습니다.");
        } catch (InterviewIntegrationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Anthropic API 호출 실패", e);
            throw new InterviewIntegrationException("AI 서비스에 연결하지 못했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}
