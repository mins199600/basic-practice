package com.practice.logincrud.codingtest;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Anthropic Claude API를 호출해 코딩테스트 문제 생성 / 답안 첨삭 / 다국어 모범답안을 만드는 서비스.
 * interview 패키지의 ClaudeAiService와 동일하게 anthropicRestClient 빈을 공용으로 사용한다.
 *
 * 주의: 실제로 코드를 실행/채점하지 않는다 (샌드박스 없음). AI가 코드를 "읽고" 텍스트로 첨삭하는 방식이며,
 * 화면/설명 문구에도 이 한계를 명시해 사용자가 오해하지 않도록 한다.
 */
@Service
@Slf4j
public class CodingTestAiService {

    private final RestClient anthropicRestClient;
    private final String model;

    public CodingTestAiService(@Qualifier("anthropicRestClient") RestClient anthropicRestClient,
                                @Value("${anthropic.model:claude-sonnet-4-5}") String model) {
        this.anthropicRestClient = anthropicRestClient;
        this.model = model;
    }

    /** 난이도별 새 코딩테스트 문제를 생성한다 (특정 언어에 종속되지 않는 알고리즘/로직 문제). */
    public String generateProblem(String difficulty) {
        String systemPrompt = """
                너는 신입 개발자 코딩테스트 문제 출제 위원이다. 난이도 "%s" 수준의 코딩테스트 문제를 1개 출제한다.

                [출제 규칙]
                1. 특정 프로그래밍 언어에 종속되지 않는 알고리즘/로직 문제로 낸다 (Java/Python/C 어느 언어로도 풀 수 있어야 함).
                2. "기초" 난이도는 반복문/조건문/배열 수준, "초급"은 문자열/자료구조 기초, "중급"은 정렬/탐색/재귀, "고급"은 자료구조 응용/DP/그래프 수준으로 낸다.
                3. 아래 형식을 반드시 지킨다:
                   [문제]
                   (문제 설명)
                   [입력]
                   (입력 조건)
                   [출력]
                   (출력 조건)
                   [예시]
                   입력: ...
                   출력: ...
                4. 존댓말을 사용하고, 정답 코드나 풀이 힌트는 절대 포함하지 않는다.
                5. 과도한 마크다운 서식(굵게/제목 등) 없이 위 형식 그대로 일반 텍스트로 작성한다.
                """.formatted(safeDifficulty(difficulty));

        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", "위 규칙에 맞는 코딩테스트 문제를 1개 출제해줘.")
        );

        return callClaude(systemPrompt, messages, 1024);
    }

    /** 지원자가 제출한 답안 코드를 읽고 텍스트로 첨삭한다 (코드 실행 없음). */
    public String reviewAnswer(String problemText, String language, String code) {
        String systemPrompt = """
                너는 신입 개발자 코딩테스트 채점관이다. 아래 문제와 지원자가 제출한 %s 코드를 "읽고" 첨삭한다.
                실제로 코드를 실행하지 않으므로, 코드를 정적으로 분석해서 판단한다.

                [문제]
                %s

                [첨삭 규칙]
                1. 먼저 코드가 문제 요구사항을 만족하는지, 로직상 정답에 도달하는지 판단해서 "정답 여부: 정답 / 부분 정답 / 오답" 중 하나를 명시한다.
                2. 잘한 점 1~2개, 개선할 점 1~2개를 구체적으로 짚는다 (시간복잡도, 예외처리, 가독성 등).
                3. 틀렸거나 개선이 필요하면 어느 부분을 어떻게 고치면 되는지 방향을 제시한다 (전체 정답 코드를 새로 써주지는 않는다).
                4. 존댓말을 사용하고, 600자 내외로 간결하게 작성한다.
                5. 굵게(**) 정도만 사용하고 과도한 마크다운 서식은 쓰지 않는다.
                """.formatted(safeLanguage(language), problemText);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", "다음 코드를 첨삭해줘:\n\n```" + safeLanguage(language).toLowerCase() + "\n" + code + "\n```")
        );

        return callClaude(systemPrompt, messages, 1024);
    }

    /** 문제에 대한 Java/Python/C 3개 언어 모범답안을 한 번에 생성한다. */
    public String generateSolutions(String problemText) {
        String systemPrompt = """
                너는 신입 개발자 코딩테스트 모범답안 작성자이다. 아래 문제에 대한 모범답안을 Java, Python, C 3개 언어로 각각 작성한다.

                [문제]
                %s

                [작성 규칙]
                1. 아래 형식을 반드시 지킨다:
                   [Java]
                   (코드)
                   [Python]
                   (코드)
                   [C]
                   (코드)
                2. 각 언어 코드는 그대로 컴파일/실행 가능한 완전한 형태로 작성한다 (입출력 처리 포함).
                3. 코드 블록 안에 핵심 로직에는 짧은 한글 주석을 붙인다.
                4. 코드 외의 부가 설명은 최소화한다.
                """.formatted(problemText);

        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", "위 문제의 Java, Python, C 모범답안을 각각 작성해줘.")
        );

        return callClaude(systemPrompt, messages, 2048);
    }

    private String safeDifficulty(String difficulty) {
        return (difficulty == null || difficulty.isBlank()) ? "기초" : difficulty;
    }

    private String safeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "JAVA";
        }
        return language.toUpperCase();
    }

    private String callClaude(String systemPrompt, List<Map<String, String>> messages, int maxTokens) {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("system", systemPrompt);
        requestBody.put("messages", messages);

        try {
            JsonNode response = anthropicRestClient.post()
                    .uri("/v1/messages")
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.has("content") || response.path("content").isEmpty()) {
                throw new CodingTestException("AI 응답을 받지 못했습니다. 잠시 후 다시 시도해주세요.");
            }

            return response.path("content").get(0).path("text").asText("").trim();

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Anthropic API 인증 실패 — ANTHROPIC_API_KEY 확인 필요", e);
            throw new CodingTestException("AI 서비스 인증에 실패했습니다. 관리자에게 문의해주세요.");
        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("Anthropic API rate limit 초과");
            throw new CodingTestException("AI 요청이 많아 잠시 지연되고 있습니다. 잠시 후 다시 시도해주세요.");
        } catch (HttpClientErrorException e) {
            log.error("Anthropic API 오류 status={} body={}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new CodingTestException("AI 응답 생성 중 오류가 발생했습니다.");
        } catch (CodingTestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Anthropic API 호출 실패", e);
            throw new CodingTestException("AI 서비스에 연결하지 못했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}
