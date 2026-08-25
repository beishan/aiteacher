INSERT INTO system_settings (key, value, description) VALUES
('ui.site_icon_url', '', '网站图标地址')
ON CONFLICT (key) DO NOTHING;
