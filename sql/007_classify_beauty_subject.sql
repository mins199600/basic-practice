-- =============================================
-- 피부자격증(certification_id=3) 문제 분류: id 99~157 → 피부미용학
-- =============================================

UPDATE certification_question q
    JOIN subject s ON s.certification_id = q.certification_id AND s.name = '피부미용학'
SET q.subject_id = s.id
WHERE q.certification_id = 3
  AND q.id BETWEEN 99 AND 157;

-- =============================================
-- 검증용 쿼리
-- =============================================
-- SELECT s.name, COUNT(*) FROM certification_question q
--   JOIN subject s ON s.id = q.subject_id
--  WHERE q.certification_id = 3 GROUP BY s.name;
--
-- SELECT COUNT(*) FROM certification_question
--  WHERE certification_id = 3 AND id BETWEEN 99 AND 157 AND subject_id IS NULL;

-- =============================================
-- 롤백
-- =============================================
-- UPDATE certification_question SET subject_id = NULL WHERE certification_id = 3 AND id BETWEEN 99 AND 157;
