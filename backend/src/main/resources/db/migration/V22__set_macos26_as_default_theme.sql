INSERT INTO system_settings (key, value, description)
VALUES ('ui.theme', 'macos26', '界面主题')
ON CONFLICT (key) DO UPDATE
SET value = 'macos26',
    updated_at = CURRENT_TIMESTAMP
WHERE system_settings.value = 'default';
