-- =============================================
-- 로컬 전용 보정: 피부자격증 certification_id 불일치 수정
-- 원인: 005 마이그레이션이 EC2 기준(피부자격증=1번)으로 subject.certification_id를
--      하드코딩했는데, 로컬은 피부자격증이 3번이라 엉뚱하게 매칭됨.
--      + 최근 추가한 문제 18건(id 81~98)이 1번(정보처리기사)에 잘못 연결됨.
-- =============================================

-- 1) subject 테이블이 가리키는 자격증을 실제 피부자격증(로컬 기준 3번)으로 정정
UPDATE subject SET certification_id = 3 WHERE certification_id = 1;

-- 2) 1번(정보처리기사)에 잘못 걸려있던 문제 18건을 3번(피부자격증)으로 이동
UPDATE certification_question SET certification_id = 3 WHERE certification_id = 1;

-- 3) 3번 자격증의 전체 문제(기존 78건 + 이동된 18건 = 96건)를 id 범위 기준으로 재분류
UPDATE certification_question q
    JOIN subject s ON s.certification_id = q.certification_id AND s.name = '해부생리학'
SET q.subject_id = s.id
WHERE q.certification_id = 3
  AND q.id BETWEEN 2 AND 38;

UPDATE certification_question q
    JOIN subject s ON s.certification_id = q.certification_id AND s.name = '피부학'
SET q.subject_id = s.id
WHERE q.certification_id = 3
  AND q.id BETWEEN 39 AND 98;

-- =============================================
-- 검증용 쿼리 (실행 후 직접 확인)
-- =============================================
-- SELECT s.name, COUNT(*) FROM certification_question q
--   JOIN subject s ON s.id = q.subject_id
--  WHERE q.certification_id = 3 GROUP BY s.name;
--
-- SELECT COUNT(*) FROM certification_question
--  WHERE certification_id = 3 AND subject_id IS NULL;

-- =============================================
-- 롤백 (실행 전 mysqldump로 백업 권장)
-- =============================================
-- UPDATE certification_question SET certification_id = 1 WHERE id BETWEEN 81 AND 98;
-- UPDATE subject SET certification_id = 1 WHERE certification_id = 3;
