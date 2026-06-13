ALTER TABLE users
    ADD COLUMN user_type VARCHAR(20) NULL AFTER social_type;

UPDATE users
SET user_type = 'USER'
WHERE user_type IS NULL;

ALTER TABLE users
    MODIFY COLUMN user_type VARCHAR(20) NOT NULL DEFAULT 'USER';

-- 관리자 승격 예시
-- UPDATE users
-- SET user_type = 'ADMIN'
-- WHERE email IN ('admin1@example.com', 'admin2@example.com');

-- 확인용
-- SELECT user_id, email, nickname, user_type
-- FROM users
-- ORDER BY user_id;
