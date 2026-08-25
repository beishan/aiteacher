package com.tutorassist.ai.service;

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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DockIconService {

    private static final Set<String> ICON_NAMES = Set.of(
            "dashboard", "students", "schedule", "homework", "classrooms", "grades",
            "finance", "materials", "ai-chat", "statistics", "backup", "settings"
    );
    private static final Set<String> ALLOWED_TYPES = Set.of("image/png", "image/jpeg");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final int OUTPUT_SIZE = 512;

    private final LocalFileStorageService fileStorageService;

    public Map<String, String> getIconUrls() {
        Map<String, String> icons = new LinkedHashMap<>();
        ICON_NAMES.stream().sorted().forEach(name -> {
            String path = iconPath(name);
            if (fileStorageService.exists(path)) {
                icons.put(name, iconUrl(name, fileStorageService.lastModifiedMillis(path)));
            }
        });
        return icons;
    }

    public String upload(String name, MultipartFile file) {
        validateName(name);
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的 Dock 图标");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("图标图片不能超过 5MB");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException("仅支持 PNG 和 JPG 图片");
        }

        BufferedImage source;
        try (InputStream inputStream = file.getInputStream()) {
            source = ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new BusinessException("图标图片读取失败");
        }
        if (source == null || source.getWidth() < 32 || source.getHeight() < 32) {
            throw new BusinessException("图标图片无效或尺寸小于 32×32");
        }
        if (source.getWidth() > 8192 || source.getHeight() > 8192) {
            throw new BusinessException("图标图片尺寸不能超过 8192×8192");
        }

        String path = iconPath(name);
        fileStorageService.saveFile(path, normalize(source));
        return iconUrl(name, fileStorageService.lastModifiedMillis(path));
    }

    public void remove(String name) {
        validateName(name);
        fileStorageService.deleteFile(iconPath(name));
    }

    public boolean exists(String name) {
        validateName(name);
        return fileStorageService.exists(iconPath(name));
    }

    public InputStream getIcon(String name) {
        validateName(name);
        return fileStorageService.getFile(iconPath(name));
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
            throw new IllegalStateException("Dock 图标处理失败", e);
        }
    }

    private void validateName(String name) {
        if (!ICON_NAMES.contains(name)) {
            throw new BusinessException("无效的 Dock 图标名称");
        }
    }

    private String iconPath(String name) {
        return "branding/dock-icons/" + name + ".png";
    }

    private String iconUrl(String name, long version) {
        return "/api/v1/settings/dock-icons/" + name + "?v=" + version;
    }
}
