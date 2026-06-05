-- 课程排课表
CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT REFERENCES students(id),
    class_id BIGINT,
    subject VARCHAR(50) NOT NULL,
    course_type VARCHAR(20) NOT NULL,
    title VARCHAR(100),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    location VARCHAR(200),
    meeting_link VARCHAR(500),
    recurrence_rule VARCHAR(200),
    recurrence_end_date DATE,
    parent_course_id BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    remark TEXT,
    color VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE courses IS '课程排课表';
COMMENT ON COLUMN courses.student_id IS '学生ID（一对一/网课）';
COMMENT ON COLUMN courses.class_id IS '虚拟班级ID（小班课）';
COMMENT ON COLUMN courses.subject IS '科目';
COMMENT ON COLUMN courses.course_type IS '课程类型：ONE_ON_ONE-一对一, ONLINE-网课, SMALL_CLASS-小班课';
COMMENT ON COLUMN courses.title IS '课程标题';
COMMENT ON COLUMN courses.start_time IS '开始时间';
COMMENT ON COLUMN courses.end_time IS '结束时间';
COMMENT ON COLUMN courses.location IS '上课地点';
COMMENT ON COLUMN courses.meeting_link IS '网课会议链接';
COMMENT ON COLUMN courses.recurrence_rule IS '重复规则（iCal RRULE 格式）';
COMMENT ON COLUMN courses.recurrence_end_date IS '周期排课截止日期';
COMMENT ON COLUMN courses.parent_course_id IS '父课程ID（周期排课生成的子课程）';
COMMENT ON COLUMN courses.status IS '状态：SCHEDULED-已排课, COMPLETED-已完成, CANCELLED-已取消';
COMMENT ON COLUMN courses.remark IS '备注';
COMMENT ON COLUMN courses.color IS '日历显示颜色';
COMMENT ON COLUMN courses.deleted IS '逻辑删除标记';

CREATE INDEX idx_courses_student_id ON courses(student_id) WHERE deleted = FALSE;
CREATE INDEX idx_courses_class_id ON courses(class_id) WHERE deleted = FALSE;
CREATE INDEX idx_courses_start_time ON courses(start_time) WHERE deleted = FALSE;
CREATE INDEX idx_courses_status ON courses(status) WHERE deleted = FALSE;
CREATE INDEX idx_courses_parent_id ON courses(parent_course_id) WHERE deleted = FALSE;
