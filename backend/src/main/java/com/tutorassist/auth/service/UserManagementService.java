package com.tutorassist.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorassist.auth.dto.CreateUserRequest;
import com.tutorassist.auth.dto.SystemUserVO;
import com.tutorassist.auth.entity.User;
import com.tutorassist.auth.mapper.UserMapper;
import com.tutorassist.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private static final String ADMIN_ROLE = "ADMIN";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public List<SystemUserVO> listUsers(Long operatorId) {
        requireAdmin(operatorId);
        return userMapper.selectList(new LambdaQueryWrapper<User>().orderByAsc(User::getId))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Transactional
    public SystemUserVO createUser(Long operatorId, CreateUserRequest request) {
        requireAdmin(operatorId);
        String username = request.getUsername().trim();
        Long existingCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        if (existingCount != null && existingCount > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setDisplayName(request.getDisplayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(ADMIN_ROLE);
        user.setEnabled(true);
        userMapper.insert(user);
        return toVO(user);
    }

    @Transactional
    public void resetPassword(Long operatorId, Long userId, String newPassword) {
        requireAdmin(operatorId);
        User target = requireUser(userId);
        target.setPasswordHash(passwordEncoder.encode(newPassword));
        userMapper.updateById(target);
    }

    @Transactional
    public void updateEnabled(Long operatorId, Long userId, boolean enabled) {
        requireAdmin(operatorId);
        User target = requireUser(userId);
        if (!enabled && operatorId.equals(userId)) {
            throw new BusinessException("不能禁用当前登录账号");
        }
        if (!enabled && ADMIN_ROLE.equals(target.getRole()) && countEnabledAdmins() <= 1) {
            throw new BusinessException("系统至少需要保留一个已启用的管理员账号");
        }
        target.setEnabled(enabled);
        userMapper.updateById(target);
    }

    private User requireAdmin(Long userId) {
        User user = requireUser(userId);
        if (!Boolean.TRUE.equals(user.getEnabled()) || !ADMIN_ROLE.equals(user.getRole())) {
            throw new BusinessException(403, "仅系统管理员可执行此操作");
        }
        return user;
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private long countEnabledAdmins() {
        return userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getRole, ADMIN_ROLE)
                .eq(User::getEnabled, true));
    }

    private SystemUserVO toVO(User user) {
        return SystemUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
