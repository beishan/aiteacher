-- AI 调用记录表
CREATE TABLE ai_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    model_name VARCHAR(50) NOT NULL,
    function_type VARCHAR(50) NOT NULL,
    input_tokens INT,
    output_tokens INT,
    duration_ms INT,
    request_text TEXT,
    response_text TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE ai_records IS 'AI 调用记录表';
COMMENT ON COLUMN ai_records.user_id IS '用户ID';
COMMENT ON COLUMN ai_records.model_name IS '模型名称：claude, gpt-4o, qwen 等';
COMMENT ON COLUMN ai_records.function_type IS '功能类型：HOMEWORK_GRADE-批改作业, TEACHING_PLAN-教学计划, STUDENT_PROFILE-学生档案, CHAT-智能问答';
COMMENT ON COLUMN ai_records.input_tokens IS '输入 Token 数';
COMMENT ON COLUMN ai_records.output_tokens IS '输出 Token 数';
COMMENT ON COLUMN ai_records.duration_ms IS '响应耗时（毫秒）';
COMMENT ON COLUMN ai_records.request_text IS '请求内容';
COMMENT ON COLUMN ai_records.response_text IS '响应内容';
COMMENT ON COLUMN ai_records.deleted IS '逻辑删除标记';

CREATE INDEX idx_ai_records_user_id ON ai_records(user_id) WHERE deleted = FALSE;
CREATE INDEX idx_ai_records_function_type ON ai_records(function_type) WHERE deleted = FALSE;
CREATE INDEX idx_ai_records_created_at ON ai_records(created_at) WHERE deleted = FALSE;

-- 系统设置表
CREATE TABLE system_settings (
    id BIGSERIAL PRIMARY KEY,
    key VARCHAR(100) NOT NULL UNIQUE,
    value TEXT,
    description VARCHAR(500),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE system_settings IS '系统设置表';
COMMENT ON COLUMN system_settings.key IS '设置键';
COMMENT ON COLUMN system_settings.value IS '设置值';
COMMENT ON COLUMN system_settings.description IS '设置描述';
COMMENT ON COLUMN system_settings.updated_at IS '更新时间';

-- 初始化默认设置
INSERT INTO system_settings (key, value, description) VALUES
('ai.default_model', 'claude', '默认 AI 模型'),
('ai.claude.api_key', '', 'Claude API Key'),
('ai.claude.model', 'claude-sonnet-4-20250514', 'Claude 模型版本'),
('ai.openai.api_key', '', 'OpenAI API Key'),
('ai.openai.model', 'gpt-4o', 'OpenAI 模型版本'),
('ai.openai.base_url', 'https://api.openai.com/v1', 'OpenAI API 地址'),
('ai.ollama.base_url', 'http://localhost:11434', 'Ollama API 地址'),
('ai.ollama.model', 'qwen2.5', 'Ollama 模型版本'),
('notification.wechat_webhook', '', '企业微信 Webhook 地址'),
('notification.reminder_minutes', '30', '课程提醒提前分钟数');
