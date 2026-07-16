package com.practice.logincrud.interview;

/**
 * GitHub API / Claude API 연동 중 발생하는 예외를 감싸는 공통 예외.
 * 컨트롤러에서 잡아 사용자에게는 message()를 그대로 보여주고, 원인은 로그로만 남긴다.
 */
public class InterviewIntegrationException extends RuntimeException {
    public InterviewIntegrationException(String message) {
        super(message);
    }

    public InterviewIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
