package com.tutorassist.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorassist.ai.entity.SystemSetting;
import com.tutorassist.ai.mapper.SystemSettingMapper;
import com.tutorassist.common.exception.BusinessException;
import com.tutorassist.material.service.LocalFileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SiteIconService {

    private static final String ICON_PATH = "branding/site-icon.png";
    private static final String SETTING_KEY = "ui.site_icon_url";
    private static final int OUTPUT_SIZE = 512;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/png", "image/jpeg");

    private final LocalFileStorageService fileStorageService;
    private final SystemSettingMapper settingMapper;

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的网站图标");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("图片大小不能超过 5MB");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException("上传文件必须是 PNG 或 JPG 图片");
        }

        BufferedImage source;
        try (InputStream inputStream = file.getInputStream()) {
            source = ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new BusinessException("图片读取失败");
        }
        if (source == null || source.getWidth() < 32 || source.getHeight() < 32) {
            throw new BusinessException("图片无效或尺寸小于 32×32");
        }
        if (source.getWidth() > 8192 || source.getHeight() > 8192) {
            throw new BusinessException("图片尺寸不能超过 8192×8192");
        }

        byte[] normalizedIcon = normalize(source);
        fileStorageService.saveFile(ICON_PATH, normalizedIcon);
        String iconUrl = "/api/v1/settings/site-icon?v=" + Instant.now().toEpochMilli();
        updateSetting(iconUrl);
        return iconUrl;
    }

    public boolean exists() {
        return fileStorageService.exists(ICON_PATH);
    }

    public InputStream getIcon() {
        return fileStorageService.getFile(ICON_PATH);
    }

    private byte[] normalize(BufferedImage source) {
        int side = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - side) / 2;
        int y = (source.getHeight() - side) / 2;
        BufferedImage output = new BufferedImage(OUTPUT_SIZE, OUTPUT_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = output.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(source, 0, 0, OUTPUT_SIZE, OUTPUT_SIZE, x, y, x + side, y + side, null);
        } finally {
            graphics.dispose();
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(output, "png", outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("网站图标处理失败", e);
        }
    }

    private void updateSetting(String iconUrl) {
        LambdaQueryWrapper<SystemSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemSetting::getKey, SETTING_KEY);
        SystemSetting setting = settingMapper.selectOne(wrapper);
        if (setting == null) {
            setting = new SystemSetting();
            setting.setKey(SETTING_KEY);
            setting.setDescription("网站图标地址");
            setting.setValue(iconUrl);
            setting.setUpdatedAt(LocalDateTime.now());
            settingMapper.insert(setting);
            return;
        }
        setting.setValue(iconUrl);
        setting.setUpdatedAt(LocalDateTime.now());
        settingMapper.updateById(setting);
    }
}
