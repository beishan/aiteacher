-- 通知表
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    recipient_type VARCHAR(20),
    recipient_id BIGINT,
    channel VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'UNREAD',
    send_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE notifications IS '通知表';
COMMENT ON COLUMN notifications.type IS '通知类型：COURSE_REMINDER-课程提醒, HOMEWORK_DUE-作业到期, FEE_DUE-费用到期, SYSTEM-系统通知';
COMMENT ON COLUMN notifications.title IS '通知标题';
COMMENT ON COLUMN notifications.content IS '通知内容';
COMMENT ON COLUMN notifications.recipient_type IS '接收者类型：USER-用户, STUDENT-学生, PARENT-家长';
COMMENT ON COLUMN notifications.recipient_id IS '接收者ID';
COMMENT ON COLUMN notifications.channel IS '通知渠道：IN_APP-站内, WECHAT-企业微信, EMAIL-邮件';
COMMENT ON COLUMN notifications.status IS '状态：UNREAD-未读, READ-已读, SENT-已发送, FAILED-发送失败';
COMMENT ON COLUMN notifications.send_time IS '发送时间';
COMMENT ON COLUMN notifications.deleted IS '逻辑删除标记';

CREATE INDEX idx_notifications_recipient ON notifications(recipient_type, recipient_id) WHERE deleted = FALSE;
CREATE INDEX idx_notifications_status ON notifications(status) WHERE deleted = FALSE;
CREATE INDEX idx_notifications_type ON notifications(type) WHERE deleted = FALSE;
CREATE INDEX idx_notifications_created_at ON notifications(created_at) WHERE deleted = FALSE;
