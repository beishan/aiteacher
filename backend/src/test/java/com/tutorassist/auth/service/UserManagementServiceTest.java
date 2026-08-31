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
        assertEquals("ADMIN", inserted.getRole());
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

    private CreateUserRequest request(String username, String displayName, String password) {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername(username);
        request.setDisplayName(displayName);
        request.setPassword(password);
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
