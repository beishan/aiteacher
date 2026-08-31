package com.tutorassist.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorassist.auth.dto.CreateUserRequest;
import com.tutorassist.auth.dto.SystemUserVO;
import com.tutorassist.auth.entity.User;
import com.tutorassist.auth.mapper.UserMapper;
import com.tutorassist.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserManagementServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserManagementService service;

    @BeforeEach
    void setUp() {
        service = new UserManagementService(userMapper, passwordEncoder);
    }

    @Test
    void createUser_CreatesEnabledAdminWithEncodedPassword() {
        User operator = enabledAdmin(1L);
        when(userMapper.selectById(1L)).thenReturn(operator);
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-password");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return 1;
        });

        SystemUserVO result = service.createUser(1L, request("teacher.li", "李老师", "secret123"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        User inserted = captor.getValue();
        assertEquals("teacher.li", inserted.getUsername());
        assertEquals("李老师", inserted.getDisplayName());
        assertEquals("encoded-password", inserted.getPasswordHash());
        assertEquals("TEACHER", inserted.getRole());
        assertTrue(inserted.getEnabled());
        assertEquals(2L, result.getId());
        assertEquals("teacher.li", result.getUsername());
    }

    @Test
    void createUser_RejectsDuplicateUsername() {
        when(userMapper.selectById(1L)).thenReturn(enabledAdmin(1L));
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.createUser(1L, request("admin", "另一位管理员", "secret123")));

        assertEquals("用户名已存在", exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userMapper, never()).insert(any());
    }

    @Test
    void createUser_RejectsNonAdminOperator() {
        User operator = enabledAdmin(1L);
        operator.setRole("USER");
        when(userMapper.selectById(1L)).thenReturn(operator);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.createUser(1L, request("teacher.li", "李老师", "secret123")));

        assertEquals(403, exception.getCode());
        verify(userMapper, never()).insert(any());
    }

    @Test
    void updateRole_ChangesAnotherUsersRole() {
        User target = enabledAdmin(2L);
        when(userMapper.selectById(1L)).thenReturn(enabledAdmin(1L));
        when(userMapper.selectById(2L)).thenReturn(target);
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        service.updateRole(1L, 2L, "teacher");

        assertEquals("TEACHER", target.getRole());
        verify(userMapper).updateById(target);
    }

    @Test
    void updateRole_RejectsChangingOwnRole() {
        when(userMapper.selectById(1L)).thenReturn(enabledAdmin(1L));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.updateRole(1L, 1L, "VIEWER"));

        assertEquals("不能修改当前登录账号的角色", exception.getMessage());
        verify(userMapper, never()).updateById(any());
    }

    @Test
    void updateRole_PreservesLastEnabledAdmin() {
        when(userMapper.selectById(1L)).thenReturn(enabledAdmin(1L));
        when(userMapper.selectById(2L)).thenReturn(enabledAdmin(2L));
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.updateRole(1L, 2L, "VIEWER"));

        assertEquals("系统至少需要保留一个已启用的管理员账号", exception.getMessage());
        verify(userMapper, never()).updateById(any());
    }

    private CreateUserRequest request(String username, String displayName, String password) {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(username);
        request.setDisplayName(displayName);
        request.setPassword(password);
        request.setRole("TEACHER");
        return request;
    }

    private User enabledAdmin(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("admin");
        user.setRole("ADMIN");
        user.setEnabled(true);
        return user;
    }
}
