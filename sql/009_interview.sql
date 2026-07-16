-- =============================================
-- 마이그레이션: 프로젝트 면접 준비 (GitHub 연동 + AI 질문/첨삭)
-- =============================================

-- 회원이 등록한 GitHub 프로젝트
CREATE TABLE interview_project (
    id                INT(11)       NOT NULL AUTO_INCREMENT,
    member_id         INT(11)       NOT NULL,
    repo_url          VARCHAR(500)  NOT NULL,
    repo_owner        VARCHAR(100)  NOT NULL,
    repo_name         VARCHAR(100)  NOT NULL,
    description       VARCHAR(1000) NULL,             -- GitHub API에서 가져온 저장소 설명
    primary_language  VARCHAR(50)   NULL,
    readme_text       TEXT          NULL,             -- README 발췌 (AI 프롬프트 컨텍스트로 재사용, 매 요청마다 GitHub 재조회 방지)
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted           TINYINT(1)    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_interview_project_member (member_id, deleted),
    CONSTRAINT fk_interview_project_member
        FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 면접 세션(대화방) — 프로젝트 1개에 여러 번 면접 연습 가능
CREATE TABLE interview_session (
    id             INT(11)      NOT NULL AUTO_INCREMENT,
    project_id     INT(11)      NOT NULL,
    member_id      INT(11)      NOT NULL,             -- 조회 시 매번 project 조인 안 해도 되도록 비정규화
    status         VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS',  -- IN_PROGRESS / ENDED
    question_count INT(11)      NOT NULL DEFAULT 0,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at       DATETIME     NULL,
    PRIMARY KEY (id),
    KEY idx_interview_session_project (project_id),
    KEY idx_interview_session_member (member_id),
    CONSTRAINT fk_interview_session_project
        FOREIGN KEY (project_id) REFERENCES interview_project (id),
    CONSTRAINT fk_interview_session_member
        FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 면접 대화 메시지 (질문 / 답변 / 첨삭)
CREATE TABLE interview_message (
    id            INT(11)      NOT NULL AUTO_INCREMENT,
    session_id    INT(11)      NOT NULL,
    sender        VARCHAR(10)  NOT NULL,               -- AI / USER
    message_type  VARCHAR(20)  NOT NULL,                -- QUESTION / ANSWER / FEEDBACK
    content       TEXT         NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_interview_message_session (session_id, created_at),
    CONSTRAINT fk_interview_message_session
        FOREIGN KEY (session_id) REFERENCES interview_session (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 롤백 SQL (역순으로 삭제 — FK 때문에 자식 테이블부터)
-- =============================================
-- DROP TABLE IF EXISTS interview_message;
-- DROP TABLE IF EXISTS interview_session;
-- DROP TABLE IF EXISTS interview_project;
