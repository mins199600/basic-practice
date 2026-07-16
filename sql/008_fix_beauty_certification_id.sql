-- =============================================
-- 99~157번 문제가 1번(정보처리기사)에 잘못 들어간 것을 3번(피부자격증)으로 이동 + 피부미용학 분류
-- =============================================

UPDATE certification_question
SET certification_id = 3
WHERE certification_id = 1
  AND id BETWEEN 99 AND 157;

UPDATE certification_question q
    JOIN subject s ON s.certification_id = q.certification_id AND s.name = '피부미용학'
SET q.subject_id = s.id
WHERE q.certification_id = 3
  AND q.id BETWEEN 99 AND 157;

-- 검증용
-- SELECT s.name, COUNT(*) FROM certification_question q
--   JOIN subject s ON s.id = q.subject_id
--  WHERE q.certification_id = 3 GROUP BY s.name;

-- 롤백
-- UPDATE certification_question SET certification_id = 1, subject_id = NULL WHERE id BETWEEN 99 AND 157;
