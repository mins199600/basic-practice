-- =============================================
-- 마이그레이션: 자격증 문제풀이 (오답 가중치 출제)
-- =============================================

-- 문제 은행: certification.id 기준으로 문제를 붙인다.
-- (cert_name = '피부자격증' / 'SQLD' 등 certification 행은 이미 존재한다고 가정, 문제 데이터는 직접 insert)
CREATE TABLE certification_question (
    id                INT(11)       NOT NULL AUTO_INCREMENT,
    certification_id  INT(11)       NOT NULL,
    question_text     VARCHAR(1000) NOT NULL,
    choice1           VARCHAR(255)  NOT NULL,
    choice2           VARCHAR(255)  NOT NULL,
    choice3           VARCHAR(255)  NOT NULL,
    choice4           VARCHAR(255)  NOT NULL,
    answer_no         TINYINT(1)    NOT NULL,        -- 정답 번호 1~4
    explanation       VARCHAR(1000) NULL,             -- 오답 시 보여줄 해설
    created_at        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted           TINYINT(1)    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_certification_question_cert (certification_id, deleted),
    CONSTRAINT fk_certification_question_cert
        FOREIGN KEY (certification_id) REFERENCES certification (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 회원별 문제 통계: 출제 가중치(weight)와 정오답 횟수를 저장
-- weight 기본 1 / 오답 +2 / 정답 -1(최소 1로 clamp) → INSERT ... ON DUPLICATE KEY UPDATE 로 upsert
CREATE TABLE member_question_stat (
    id                INT(11)  NOT NULL AUTO_INCREMENT,
    member_id         INT(11)  NOT NULL,
    question_id       INT(11)  NOT NULL,
    weight            INT      NOT NULL DEFAULT 1,
    correct_count     INT      NOT NULL DEFAULT 0,
    wrong_count       INT      NOT NULL DEFAULT 0,
    last_answered_at  DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_member_question (member_id, question_id),   -- upsert 기준 unique key
    KEY idx_member_question_stat_member (member_id),
    CONSTRAINT fk_member_question_stat_member
        FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT fk_member_question_stat_question
        FOREIGN KEY (question_id) REFERENCES certification_question (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =============================================
-- 롤백 SQL (역순으로 삭제 — FK 때문에 자식 테이블부터)
-- =============================================
-- DROP TABLE IF EXISTS member_question_stat;
-- DROP TABLE IF EXISTS certification_question;
