-- 虚拟班级表
CREATE TABLE virtual_classes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    subject VARCHAR(50),
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE virtual_classes IS '虚拟班级表';
COMMENT ON COLUMN virtual_classes.name IS '班级名称';
COMMENT ON COLUMN virtual_classes.subject IS '科目';
COMMENT ON COLUMN virtual_classes.description IS '班级描述';
COMMENT ON COLUMN virtual_classes.status IS '状态：ACTIVE-开课中, ENDED-已结束';
COMMENT ON COLUMN virtual_classes.deleted IS '逻辑删除标记';

-- 班级成员表
CREATE TABLE class_members (
    id BIGSERIAL PRIMARY KEY,
    class_id BIGINT NOT NULL REFERENCES virtual_classes(id),
    student_id BIGINT NOT NULL REFERENCES students(id),
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(class_id, student_id)
);

COMMENT ON TABLE class_members IS '班级成员表';
COMMENT ON COLUMN class_members.class_id IS '班级ID';
COMMENT ON COLUMN class_members.student_id IS '学生ID';
COMMENT ON COLUMN class_members.joined_at IS '加入时间';
COMMENT ON COLUMN class_members.deleted IS '逻辑删除标记';

CREATE INDEX idx_class_members_class_id ON class_members(class_id) WHERE deleted = FALSE;
CREATE INDEX idx_class_members_student_id ON class_members(student_id) WHERE deleted = FALSE;
