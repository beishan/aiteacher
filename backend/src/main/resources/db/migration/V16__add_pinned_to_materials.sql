-- 为资料表添加置顶字段
ALTER TABLE materials ADD COLUMN is_pinned BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN materials.is_pinned IS '是否置顶';
