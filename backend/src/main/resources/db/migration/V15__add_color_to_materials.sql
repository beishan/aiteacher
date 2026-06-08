-- 为资料表添加颜色字段（用于文件夹颜色标识）
ALTER TABLE materials ADD COLUMN color VARCHAR(20);

COMMENT ON COLUMN materials.color IS '文件夹颜色标识';
