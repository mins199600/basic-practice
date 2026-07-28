-- =============================================
-- 마이그레이션: 코딩테스트 (AI 문제 생성 + AI 첨삭 + 다국어 모범답안)
-- =============================================

-- 회원이 AI로부터 받은 코딩테스트 문제 1건 = 세션 1건
CREATE TABLE coding_test_session (
    id           INT(11)      NOT NULL AUTO_INCREMENT,
    member_id    INT(11)      NOT NULL,
    difficulty   VARCHAR(20)  NOT NULL,                     -- 기초 / 초급 / 중급 / 고급
    problem_text TEXT         NOT NULL,                     -- AI가 생성한 문제 원문 (특정 언어에 종속되지 않음)
    status       VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS', -- IN_PROGRESS(문제만 있음) / ANSWERED(답안 제출+첨삭 완료)
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_coding_test_session_member (member_id, created_at),
    CONSTRAINT fk_coding_test_session_member
        FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 문제/답안/첨삭/모범답안 메시지
CREATE TABLE coding_test_message (
    id            INT(11)      NOT NULL AUTO_INCREMENT,
    session_id    INT(11)      NOT NULL,
    sender        VARCHAR(10)  NOT NULL,                    -- AI / USER
    message_type  VARCHAR(20)  NOT NULL,                    -- PROBLEM / ANSWER / FEEDBACK / SOLUTIONS
    language      VARCHAR(20)  NULL,                        -- ANSWER일 때만 채움: JAVA / PYTHON / C
    content       TEXT         NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_coding_test_message_session (session_id, created_at),
    CONSTRAINT fk_coding_test_message_session
        FOREIGN KEY (session_id) REFERENCES coding_test_session (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 롤백 SQL (역순 — FK 때문에 자식 테이블부터)
-- =============================================
-- DROP TABLE IF EXISTS coding_test_message;
-- DROP TABLE IF EXISTS coding_test_session;
