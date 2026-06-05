-- 学生表
CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(10),
    birth_date DATE,
    grade VARCHAR(20) NOT NULL,
    school VARCHAR(100),
    subjects VARCHAR(200),
    source VARCHAR(50),
    address VARCHAR(500),
    phone VARCHAR(30),
    parent_name VARCHAR(50),
    parent_phone VARCHAR(30) NOT NULL,
    parent_relation VARCHAR(20),
    remark TEXT,
    enrollment_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    avatar_url VARCHAR(500),
    tags VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE students IS '学生表';
COMMENT ON COLUMN students.name IS '学生姓名';
COMMENT ON COLUMN students.gender IS '性别：MALE-男，FEMALE-女，OTHER-其他';
COMMENT ON COLUMN students.birth_date IS '出生日期';
COMMENT ON COLUMN students.grade IS '年级：PRIMARY_1~6-小学1~6年级，JUNIOR_1~3-初一~高三，SENIOR_1~3-高一~高三';
COMMENT ON COLUMN students.school IS '就读学校';
COMMENT ON COLUMN students.subjects IS '学习科目，JSON数组';
COMMENT ON COLUMN students.source IS '学生来源：REFERRAL-家长推荐，GROUP-群推荐，PLATFORM-平台引流，OTHER-其他';
COMMENT ON COLUMN students.address IS '家庭住址';
COMMENT ON COLUMN students.phone IS '学生联系方式';
COMMENT ON COLUMN students.parent_name IS '家长姓名';
COMMENT ON COLUMN students.parent_phone IS '家长联系方式';
COMMENT ON COLUMN students.parent_relation IS '家长关系：FATHER-父亲，MOTHER-母亲，GUARDIAN-其他监护人';
COMMENT ON COLUMN students.remark IS '备注';
COMMENT ON COLUMN students.enrollment_date IS '入学日期';
COMMENT ON COLUMN students.status IS '状态：ACTIVE-在读，PAUSED-暂停，GRADUATED-毕业，LOST-流失';
COMMENT ON COLUMN students.avatar_url IS '学生头像URL';
COMMENT ON COLUMN students.tags IS '标签，JSON数组';
COMMENT ON COLUMN students.deleted IS '逻辑删除标记';

CREATE INDEX idx_students_status ON students(status) WHERE deleted = FALSE;
CREATE INDEX idx_students_grade ON students(grade) WHERE deleted = FALSE;
