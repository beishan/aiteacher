ALTER TABLE users
    DROP CONSTRAINT IF EXISTS chk_users_role;

ALTER TABLE users
    ADD CONSTRAINT chk_users_role
        CHECK (role IN ('ADMIN', 'TEACHER', 'VIEWER'));

COMMENT ON COLUMN users.role IS '角色：ADMIN-管理员，TEACHER-教师，VIEWER-只读用户';
