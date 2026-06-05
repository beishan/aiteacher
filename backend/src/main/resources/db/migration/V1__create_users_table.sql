-- 用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(50),
    role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    avatar_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE users IS '用户表';
COMMENT ON COLUMN users.id IS '用户ID';
COMMENT ON COLUMN users.username IS '用户名';
COMMENT ON COLUMN users.password_hash IS '密码哈希';
COMMENT ON COLUMN users.display_name IS '显示名称';
COMMENT ON COLUMN users.role IS '角色：ADMIN-管理员';
COMMENT ON COLUMN users.avatar_url IS '头像URL';
COMMENT ON COLUMN users.deleted IS '逻辑删除标记';

-- 默认管理员账号由 DataInitializer 在应用启动时自动创建
