-- =============================================
-- 마이그레이션: 회원 프로필 사진 컬럼 추가
-- =============================================

ALTER TABLE member
    ADD COLUMN profile_image VARCHAR(255) NULL AFTER nickname;

-- =============================================
-- 롤백 SQL
-- =============================================
-- ALTER TABLE member DROP COLUMN profile_image;
