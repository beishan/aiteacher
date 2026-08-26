-- 统一科目目录
CREATE TABLE subjects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

COMMENT ON TABLE subjects IS '系统科目目录';
COMMENT ON COLUMN subjects.name IS '科目名称';
COMMENT ON COLUMN subjects.sort_order IS '显示顺序，数值越小越靠前';

CREATE UNIQUE INDEX uk_subjects_name ON subjects (LOWER(name)) WHERE deleted = FALSE;
CREATE INDEX idx_subjects_sort_order ON subjects (sort_order, id) WHERE deleted = FALSE;

INSERT INTO subjects (name, sort_order) VALUES
    ('语文', 10),
    ('数学', 20),
    ('英语', 30),
    ('物理', 40),
    ('化学', 50),
    ('生物', 60),
    ('历史', 70),
    ('政治', 80),
    ('地理', 90);

-- 将既有业务数据中的自定义科目补充到目录，避免升级后无法继续选择。
WITH used_subjects AS (
    SELECT subject FROM courses WHERE deleted = FALSE
    UNION SELECT subject FROM homeworks WHERE deleted = FALSE
    UNION SELECT subject FROM exam_records WHERE deleted = FALSE
    UNION SELECT subject FROM virtual_classes WHERE deleted = FALSE
    UNION SELECT subject FROM materials WHERE deleted = FALSE
    UNION SELECT subject FROM student_fees WHERE deleted = FALSE
), normalized_subjects AS (
    SELECT MIN(BTRIM(subject)) AS name
    FROM used_subjects
    WHERE subject IS NOT NULL AND BTRIM(subject) <> ''
    GROUP BY LOWER(BTRIM(subject))
)
INSERT INTO subjects (name, sort_order)
SELECT name, 100 + ROW_NUMBER() OVER (ORDER BY name) * 10
FROM normalized_subjects candidate
WHERE NOT EXISTS (
    SELECT 1 FROM subjects existing WHERE LOWER(existing.name) = LOWER(candidate.name) AND existing.deleted = FALSE
);
