-- 费用记录表
CREATE TABLE fee_records (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id),
    fee_id BIGINT REFERENCES student_fees(id),
    amount DECIMAL(10,2) NOT NULL,
    payment_type VARCHAR(20) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(20),
    note TEXT,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE fee_records IS '费用记录表';
COMMENT ON COLUMN fee_records.student_id IS '学生ID';
COMMENT ON COLUMN fee_records.fee_id IS '关联课时费ID';
COMMENT ON COLUMN fee_records.amount IS '金额（元）';
COMMENT ON COLUMN fee_records.payment_type IS '收支类型：INCOME-收入, REFUND-退款';
COMMENT ON COLUMN fee_records.payment_date IS '缴费日期';
COMMENT ON COLUMN fee_records.payment_method IS '支付方式：CASH-现金, WECHAT-微信, ALIPAY-支付宝, BANK-转账';
COMMENT ON COLUMN fee_records.note IS '备注';
COMMENT ON COLUMN fee_records.created_by IS '操作人ID';
COMMENT ON COLUMN fee_records.deleted IS '逻辑删除标记';

CREATE INDEX idx_fee_records_student_id ON fee_records(student_id) WHERE deleted = FALSE;
CREATE INDEX idx_fee_records_payment_date ON fee_records(payment_date) WHERE deleted = FALSE;
