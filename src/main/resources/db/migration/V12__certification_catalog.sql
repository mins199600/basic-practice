-- =============================================
-- 마이그레이션: 자격증 카탈로그(마스터) 테이블 도입
-- 배경: certification_question/subject가 회원별로 다르게 발급되는
--       certification.id 중 특정 값 하나에만 고정 연결돼 있어서,
--       그 값을 우연히 받은 회원만 문제를 풀 수 있던 구조적 결함을 해결한다.
--       (docs/reviews/2026-07-28-certification-catalog-plan-review.md,
--        TROUBLESHOOTING_2026-07-28.md 참고)
-- =============================================

-- 1) 자격증 종류 마스터 테이블
CREATE TABLE certification_catalog (
    id         INT(11)      NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_certification_catalog_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) 기존 회원 자격증명을 카탈로그로 백필 (이름 유일 제약이라 중복은 자동으로 한 번만 들어감)
INSERT IGNORE INTO certification_catalog (name)
SELECT DISTINCT TRIM(cert_name) FROM certification WHERE deleted = 0;

-- 3) certification(회원 개인 기록) - 느슨한 연결(인덱스만, 강한 FK 없음)
--    개인 기록은 카탈로그가 나중에 정리/병합돼도 깨지면 안 되므로 참조 무결성을 강제하지 않는다.
ALTER TABLE certification
    ADD COLUMN catalog_id INT(11) NULL AFTER member_id,
    ADD KEY idx_certification_catalog (catalog_id);

UPDATE certification c
    JOIN certification_catalog cat ON cat.name = TRIM(c.cert_name)
SET c.catalog_id = cat.id
WHERE c.deleted = 0;

-- 4) subject - 문제은행 성격이라 강한 FK로 카탈로그에 고정
ALTER TABLE subject
    ADD COLUMN catalog_id INT(11) NULL AFTER certification_id;

UPDATE subject s
    JOIN certification c ON c.id = s.certification_id
    JOIN certification_catalog cat ON cat.name = TRIM(c.cert_name)
SET s.catalog_id = cat.id;

ALTER TABLE subject
    MODIFY COLUMN catalog_id INT(11) NOT NULL,
    DROP FOREIGN KEY fk_subject_cert,
    DROP KEY idx_subject_cert,
    DROP COLUMN certification_id,
    ADD KEY idx_subject_catalog (catalog_id, deleted),
    ADD CONSTRAINT fk_subject_catalog
        FOREIGN KEY (catalog_id) REFERENCES certification_catalog (id);

-- 5) certification_question - 문제은행 성격이라 강한 FK로 카탈로그에 고정
ALTER TABLE certification_question
    ADD COLUMN catalog_id INT(11) NULL AFTER certification_id;

UPDATE certification_question q
    JOIN certification c ON c.id = q.certification_id
    JOIN certification_catalog cat ON cat.name = TRIM(c.cert_name)
SET q.catalog_id = cat.id;

ALTER TABLE certification_question
    MODIFY COLUMN catalog_id INT(11) NOT NULL,
    DROP FOREIGN KEY fk_certification_question_cert,
    DROP KEY idx_certification_question_cert,
    DROP COLUMN certification_id,
    ADD KEY idx_certification_question_catalog (catalog_id, deleted),
    ADD CONSTRAINT fk_certification_question_catalog
        FOREIGN KEY (catalog_id) REFERENCES certification_catalog (id);

-- =============================================
-- 검증용 쿼리 (배포 후 직접 실행해서 확인)
-- =============================================
-- 카탈로그 백필 결과
-- SELECT * FROM certification_catalog ORDER BY id;
--
-- 개인 기록 중 카탈로그 매칭 실패(고아) 건수 - 0이어야 정상(이름 편차로 실패했다면 여기서 걸림)
-- SELECT COUNT(*) FROM certification WHERE catalog_id IS NULL AND deleted = 0;
--
-- 과목/문제가 전부 카탈로그에 정상 연결됐는지
-- SELECT COUNT(*) FROM subject WHERE catalog_id IS NULL;
-- SELECT COUNT(*) FROM certification_question WHERE catalog_id IS NULL;
--
-- 서로 다른 회원이 같은 이름으로 등록해도 같은 catalog_id를 받는지
-- SELECT cert_name, catalog_id, COUNT(DISTINCT member_id) AS member_count
--   FROM certification WHERE deleted = 0 GROUP BY cert_name, catalog_id;

-- =============================================
-- 롤백 SQL (역순 — FK 때문에 자식부터)
-- =============================================
-- ALTER TABLE certification_question DROP FOREIGN KEY fk_certification_question_catalog;
-- ALTER TABLE certification_question ADD COLUMN certification_id INT(11) NULL AFTER id;
-- (증: certification_id 복구는 catalog_id -> cert_name -> certification.id 역매핑이 필요해 완전한 자동 롤백은 불가 - 백업에서 복원 권장)
-- ALTER TABLE certification_question DROP COLUMN catalog_id;
--
-- ALTER TABLE subject DROP FOREIGN KEY fk_subject_catalog;
-- ALTER TABLE subject DROP KEY idx_subject_catalog;
-- ALTER TABLE subject ADD COLUMN certification_id INT(11) NULL AFTER id;
-- ALTER TABLE subject ADD CONSTRAINT fk_subject_cert FOREIGN KEY (certification_id) REFERENCES certification (id);
-- ALTER TABLE subject DROP COLUMN catalog_id;
--
-- ALTER TABLE certification DROP KEY idx_certification_catalog;
-- ALTER TABLE certification DROP COLUMN catalog_id;
--
-- DROP TABLE IF EXISTS certification_catalog;
