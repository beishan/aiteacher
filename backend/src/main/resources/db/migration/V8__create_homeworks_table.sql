-- 作业表
CREATE TABLE homeworks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    student_id BIGINT REFERENCES students(id),
    class_id BIGINT REFERENCES virtual_classes(id),
    subject VARCHAR(50),
    content TEXT,
    due_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    score_type VARCHAR(20),
    score VARCHAR(20),
    comment TEXT,
    ai_report JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE homeworks IS '作业表';
COMMENT ON COLUMN homeworks.title IS '作业标题';
COMMENT ON COLUMN homeworks.student_id IS '学生ID（一对一作业）';
COMMENT ON COLUMN homeworks.class_id IS '班级ID（班级作业）';
COMMENT ON COLUMN homeworks.subject IS '科目';
COMMENT ON COLUMN homeworks.content IS '作业内容描述';
COMMENT ON COLUMN homeworks.due_date IS '截止日期';
COMMENT ON COLUMN homeworks.status IS '状态：PENDING-未提交, SUBMITTED-已提交, GRADED-已批改, REVIEWED-已查看';
COMMENT ON COLUMN homeworks.score_type IS '评分类型：PERCENTAGE-百分制, STAR-五星制, RANK-等级制';
COMMENT ON COLUMN homeworks.score IS '分数';
COMMENT ON COLUMN homeworks.comment IS '评语';
COMMENT ON COLUMN homeworks.ai_report IS 'AI批改报告（JSON）';
COMMENT ON COLUMN homeworks.deleted IS '逻辑删除标记';

CREATE INDEX idx_homeworks_student_id ON homeworks(student_id) WHERE deleted = FALSE;
CREATE INDEX idx_homeworks_class_id ON homeworks(class_id) WHERE deleted = FALSE;
CREATE INDEX idx_homeworks_status ON homeworks(status) WHERE deleted = FALSE;
CREATE INDEX idx_homeworks_due_date ON homeworks(due_date) WHERE deleted = FALSE;

-- 作业提交表
CREATE TABLE homework_submissions (
    id BIGSERIAL PRIMARY KEY,
    homework_id BIGINT NOT NULL REFERENCES homeworks(id),
    student_id BIGINT NOT NULL REFERENCES students(id),
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    files JSONB,
    content TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE homework_submissions IS '作业提交表';
COMMENT ON COLUMN homework_submissions.homework_id IS '作业ID';
COMMENT ON COLUMN homework_submissions.student_id IS '学生ID';
COMMENT ON COLUMN homework_submissions.submitted_at IS '提交时间';
COMMENT ON COLUMN homework_submissions.files IS '提交的文件列表（JSON）';
COMMENT ON COLUMN homework_submissions.content IS '文字内容';
COMMENT ON COLUMN homework_submissions.status IS '状态：SUBMITTED-已提交, REVIEWED-已查看';
COMMENT ON COLUMN homework_submissions.deleted IS '逻辑删除标记';

CREATE INDEX idx_homework_submissions_homework_id ON homework_submissions(homework_id) WHERE deleted = FALSE;
CREATE INDEX idx_homework_submissions_student_id ON homework_submissions(student_id) WHERE deleted = FALSE;
