INSERT INTO system_settings (key, value, description) VALUES
('ui.dock.icon_style', 'macos26', 'Dock 图标风格')
ON CONFLICT (key) DO NOTHING;
