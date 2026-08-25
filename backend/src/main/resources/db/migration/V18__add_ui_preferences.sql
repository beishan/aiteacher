INSERT INTO system_settings (key, value, description) VALUES
('ui.theme', 'default', '界面主题'),
('ui.dock.size', '58', 'macOS 26 Dock 大小'),
('ui.dock.opacity', '72', 'macOS 26 Dock 透明度'),
('ui.dock.magnification', '132', 'macOS 26 Dock 悬浮放大比例'),
('ui.dock.blur', '28', 'macOS 26 Dock 玻璃模糊强度')
ON CONFLICT (key) DO NOTHING;
