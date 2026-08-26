INSERT INTO system_settings (key, value, description)
VALUES ('ui.content_width', 'full', '界面主题内容宽度')
ON CONFLICT (key) DO NOTHING;
