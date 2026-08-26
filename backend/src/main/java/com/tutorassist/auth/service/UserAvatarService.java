package com.tutorassist.auth.service;

import com.tutorassist.auth.entity.User;
import com.tutorassist.auth.mapper.UserMapper;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserAvatarService {

    private static final int OUTPUT_SIZE = 512;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/png", "image/jpeg");

    private final LocalFileStorageService fileStorageService;
    private final UserMapper userMapper;

    public void upload(Long userId, MultipartFile file) {
        User user = getEnabledUser(userId);
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的头像");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("头像大小不能超过5MB");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException("头像必须是PNG或JPG图片");
        }

        BufferedImage source;
        try (InputStream inputStream = file.getInputStream()) {
            source = ImageIO.read(inputStream);
        } catch (IOException e) {
            throw new BusinessException("头像读取失败");
        }
        if (source == null || source.getWidth() < 32 || source.getHeight() < 32) {
            throw new BusinessException("头像无效或尺寸小于32×32");
        }
        if (source.getWidth() > 8192 || source.getHeight() > 8192) {
            throw new BusinessException("头像尺寸不能超过8192×8192");
        }

        fileStorageService.saveFile(avatarPath(userId), normalize(source));
        user.setAvatarUrl("/api/v1/auth/avatar/" + userId + "?v=" + Instant.now().toEpochMilli());
        userMapper.updateById(user);
    }

    public void remove(Long userId) {
        User user = getEnabledUser(userId);
        fileStorageService.deleteFile(avatarPath(userId));
        user.setAvatarUrl("");
        userMapper.updateById(user);
    }

    public boolean exists(Long userId) {
        return fileStorageService.exists(avatarPath(userId));
    }

    public InputStream getAvatar(Long userId) {
        return fileStorageService.getFile(avatarPath(userId));
    }

    private User getEnabledUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new BusinessException(403, "账号已被禁用，请联系系统管理员");
        }
        return user;
    }

    private String avatarPath(Long userId) {
        return "avatars/user-" + userId + ".png";
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
            throw new IllegalStateException("头像处理失败", e);
        }
    }
}
