package com.tutorassist.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutorassist.auth.entity.User;
import com.tutorassist.auth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<>());
        if (count == null || count == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setDisplayName("管理员");
            admin.setRole("ADMIN");
            userMapper.insert(admin);
            log.info("默认管理员账号已创建：admin / admin123");
        }
    }
}
