-- 学生课时费表
CREATE TABLE student_fees (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id),
    fee_type VARCHAR(20) NOT NULL,
    unit_price DECIMAL(10,2),
    prepaid_count INT,
    remaining_count INT,
    period_start DATE,
    period_end DATE,
    subject VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    remark TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE student_fees IS '学生课时费表';
COMMENT ON COLUMN student_fees.student_id IS '学生ID';
COMMENT ON COLUMN student_fees.fee_type IS '计费类型：PER_TIME-按次, PREPAID-预缴, PERIOD-按周期';
COMMENT ON COLUMN student_fees.unit_price IS '课时单价（元）';
COMMENT ON COLUMN student_fees.prepaid_count IS '预缴课时数';
COMMENT ON COLUMN student_fees.remaining_count IS '剩余课时数';
COMMENT ON COLUMN student_fees.period_start IS '周期开始日期';
COMMENT ON COLUMN student_fees.period_end IS '周期结束日期';
COMMENT ON COLUMN student_fees.subject IS '适用科目';
COMMENT ON COLUMN student_fees.status IS '状态：ACTIVE-生效, EXPIRED-过期, CANCELLED-取消';
COMMENT ON COLUMN student_fees.remark IS '备注';
COMMENT ON COLUMN student_fees.deleted IS '逻辑删除标记';

CREATE INDEX idx_student_fees_student_id ON student_fees(student_id) WHERE deleted = FALSE;
CREATE INDEX idx_student_fees_status ON student_fees(status) WHERE deleted = FALSE;
