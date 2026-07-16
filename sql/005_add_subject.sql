-- =============================================
-- 마이그레이션: 자격증 문제 과목(subject) 분류
-- =============================================

-- 자격증별 문제 과목(파트) 테이블
CREATE TABLE subject (
    id                INT(11)      NOT NULL AUTO_INCREMENT,
    certification_id  INT(11)      NOT NULL,
    name              VARCHAR(50)  NOT NULL,
    display_order     INT          NOT NULL DEFAULT 0,
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted           TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_subject_cert (certification_id, deleted),
    CONSTRAINT fk_subject_cert
        FOREIGN KEY (certification_id) REFERENCES certification (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 문제에 과목 연결 (기존 문제와의 호환을 위해 NULL 허용 → 분류 완료 후 필요하면 NOT NULL로 전환)
ALTER TABLE certification_question
    ADD COLUMN subject_id INT(11) NULL AFTER certification_id,
    ADD CONSTRAINT fk_certification_question_subject
        FOREIGN KEY (subject_id) REFERENCES subject (id);

-- 피부자격증(certification_id = 1) 6개 과목 등록
INSERT INTO subject (certification_id, name, display_order) VALUES
    (1, '해부생리학',     1),
    (1, '피부학',         2),
    (1, '피부미용학',     3),
    (1, '화장품학',       4),
    (1, '미용기기학',     5),
    (1, '공중위생관리학', 6);

-- 기존 문제 79건 분류 반영 (id 2~38 → 해부생리학 / id 39~98 → 피부학)
UPDATE certification_question q
    JOIN subject s ON s.certification_id = q.certification_id AND s.name = '해부생리학'
SET q.subject_id = s.id
WHERE q.id BETWEEN 2 AND 38;

UPDATE certification_question q
    JOIN subject s ON s.certification_id = q.certification_id AND s.name = '피부학'
SET q.subject_id = s.id
WHERE q.id BETWEEN 39 AND 98;

-- 분류 결과 확인용
-- SELECT s.name, COUNT(*) FROM certification_question q JOIN subject s ON s.id = q.subject_id GROUP BY s.name;

-- =============================================
-- 롤백 SQL (역순)
-- =============================================
-- ALTER TABLE certification_question DROP FOREIGN KEY fk_certification_question_subject;
-- ALTER TABLE certification_question DROP COLUMN subject_id;
-- DROP TABLE IF EXISTS subject;
