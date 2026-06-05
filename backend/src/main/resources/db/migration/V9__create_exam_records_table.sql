-- 考试成绩表
CREATE TABLE exam_records (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id),
    exam_name VARCHAR(200) NOT NULL,
    exam_type VARCHAR(50),
    exam_date DATE NOT NULL,
    subject VARCHAR(50) NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    full_score DECIMAL(5,2) NOT NULL DEFAULT 100,
    class_rank INT,
    grade_rank INT,
    paper_url VARCHAR(500),
    analysis TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE exam_records IS '考试成绩表';
COMMENT ON COLUMN exam_records.student_id IS '学生ID';
COMMENT ON COLUMN exam_records.exam_name IS '考试名称';
COMMENT ON COLUMN exam_records.exam_type IS '考试类型：MONTHLY-月考, MIDTERM-期中, FINAL-期末, JOINT-联考, MOCK-模拟, SELF_TEST-自测';
COMMENT ON COLUMN exam_records.exam_date IS '考试日期';
COMMENT ON COLUMN exam_records.subject IS '科目';
COMMENT ON COLUMN exam_records.score IS '分数';
COMMENT ON COLUMN exam_records.full_score IS '满分';
COMMENT ON COLUMN exam_records.class_rank IS '班级排名';
COMMENT ON COLUMN exam_records.grade_rank IS '年级排名';
COMMENT ON COLUMN exam_records.paper_url IS '试卷文件URL';
COMMENT ON COLUMN exam_records.analysis IS '分析备注';
COMMENT ON COLUMN exam_records.deleted IS '逻辑删除标记';

CREATE INDEX idx_exam_records_student_id ON exam_records(student_id) WHERE deleted = FALSE;
CREATE INDEX idx_exam_records_subject ON exam_records(subject) WHERE deleted = FALSE;
CREATE INDEX idx_exam_records_exam_date ON exam_records(exam_date) WHERE deleted = FALSE;
