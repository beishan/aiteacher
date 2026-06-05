-- 资料表
CREATE TABLE materials (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_type VARCHAR(20),
    file_size BIGINT,
    subject VARCHAR(50),
    grade VARCHAR(20),
    tags VARCHAR(500),
    parent_id BIGINT,
    is_folder BOOLEAN NOT NULL DEFAULT FALSE,
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    owner_id BIGINT,
    share_token VARCHAR(100),
    share_expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE materials IS '资料表';
COMMENT ON COLUMN materials.title IS '资料标题/文件名';
COMMENT ON COLUMN materials.file_path IS '文件路径';
COMMENT ON COLUMN materials.file_type IS '文件类型：pdf, docx, xlsx, jpg, png 等';
COMMENT ON COLUMN materials.file_size IS '文件大小（字节）';
COMMENT ON COLUMN materials.subject IS '科目';
COMMENT ON COLUMN materials.grade IS '年级';
COMMENT ON COLUMN materials.tags IS '标签（JSON数组）';
COMMENT ON COLUMN materials.parent_id IS '父目录ID';
COMMENT ON COLUMN materials.is_folder IS '是否文件夹';
COMMENT ON COLUMN materials.is_favorite IS '是否收藏';
COMMENT ON COLUMN materials.owner_id IS '所有者ID';
COMMENT ON COLUMN materials.share_token IS '分享令牌';
COMMENT ON COLUMN materials.share_expires_at IS '分享过期时间';
COMMENT ON COLUMN materials.deleted IS '逻辑删除标记';

CREATE INDEX idx_materials_parent_id ON materials(parent_id) WHERE deleted = FALSE;
CREATE INDEX idx_materials_subject ON materials(subject) WHERE deleted = FALSE;
CREATE INDEX idx_materials_is_folder ON materials(is_folder) WHERE deleted = FALSE;

-- 资料版本表
CREATE TABLE material_versions (
    id BIGSERIAL PRIMARY KEY,
    material_id BIGINT NOT NULL REFERENCES materials(id),
    version_num INT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE material_versions IS '资料版本表';
COMMENT ON COLUMN material_versions.material_id IS '资料ID';
COMMENT ON COLUMN material_versions.version_num IS '版本号';
COMMENT ON COLUMN material_versions.file_path IS '文件路径';
COMMENT ON COLUMN material_versions.file_size IS '文件大小';
COMMENT ON COLUMN material_versions.created_by IS '创建人ID';
COMMENT ON COLUMN material_versions.deleted IS '逻辑删除标记';

CREATE INDEX idx_material_versions_material_id ON material_versions(material_id) WHERE deleted = FALSE;

-- 学生专属资料表
CREATE TABLE student_materials (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES students(id),
    material_id BIGINT NOT NULL REFERENCES materials(id),
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE(student_id, material_id)
);

COMMENT ON TABLE student_materials IS '学生专属资料表';
COMMENT ON COLUMN student_materials.student_id IS '学生ID';
COMMENT ON COLUMN student_materials.material_id IS '资料ID';
COMMENT ON COLUMN student_materials.assigned_at IS '分配时间';
COMMENT ON COLUMN student_materials.deleted IS '逻辑删除标记';

CREATE INDEX idx_student_materials_student_id ON student_materials(student_id) WHERE deleted = FALSE;
CREATE INDEX idx_student_materials_material_id ON student_materials(material_id) WHERE deleted = FALSE;
