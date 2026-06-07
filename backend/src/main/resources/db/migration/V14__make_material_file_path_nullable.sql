-- 文件夹不需要 file_path，改为允许 NULL
ALTER TABLE materials ALTER COLUMN file_path DROP NOT NULL;
