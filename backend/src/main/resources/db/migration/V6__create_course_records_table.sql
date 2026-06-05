-- 上课记录表
CREATE TABLE course_records (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id),
    actual_start_time TIMESTAMP,
    actual_end_time TIMESTAMP,
    attendance_status VARCHAR(20),
    content_summary TEXT,
    homework_assigned TEXT,
    remark TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE course_records IS '上课记录表';
COMMENT ON COLUMN course_records.course_id IS '课程ID';
COMMENT ON COLUMN course_records.actual_start_time IS '实际开始时间';
COMMENT ON COLUMN course_records.actual_end_time IS '实际结束时间';
COMMENT ON COLUMN course_records.attendance_status IS '出勤状态：PRESENT-出勤, ABSENT-缺勤, LATE-迟到, LEAVE-请假';
COMMENT ON COLUMN course_records.content_summary IS '上课内容总结';
COMMENT ON COLUMN course_records.homework_assigned IS '布置的作业';
COMMENT ON COLUMN course_records.remark IS '备注';
COMMENT ON COLUMN course_records.deleted IS '逻辑删除标记';

CREATE INDEX idx_course_records_course_id ON course_records(course_id) WHERE deleted = FALSE;
