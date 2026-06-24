-- 数据备份记录表
CREATE TABLE backup_records (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(1024) NOT NULL,
    file_size BIGINT DEFAULT 0,
    backup_type VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    status VARCHAR(50) NOT NULL DEFAULT 'COMPLETED',
    duration_ms BIGINT DEFAULT 0,
    remark VARCHAR(500),
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE backup_records IS '数据备份记录表';
COMMENT ON COLUMN backup_records.backup_type IS '备份类型：MANUAL-手动, AUTO-自动';
COMMENT ON COLUMN backup_records.status IS '状态：IN_PROGRESS-进行中, COMPLETED-完成, FAILED-失败';

-- 插入自动备份配置默认值
INSERT INTO system_settings (key, value, description, updated_at) VALUES
('backup.auto.enabled', 'false', '是否启用自动备份', NOW()),
('backup.auto.cron', '0 0 2 * * ?', '自动备份 Cron 表达式（默认每天凌晨 2 点）', NOW()),
('backup.auto.max_keep', '5', '自动备份最大保留数量', NOW()),
('backup.directory', '/opt/tutor-assist/backups', '备份文件存储目录', NOW())
ON CONFLICT (key) DO NOTHING;
