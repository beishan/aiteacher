package com.tutorassist.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        HttpServletRequest request = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                request = attributes.getRequest();
            }
        } catch (Exception ignored) {
        }

        String username = "unknown";
        try {
            if (request != null && request.getUserPrincipal() != null) {
                username = request.getUserPrincipal().getName();
            }
        } catch (Exception ignored) {
        }

        Object result = null;
        try {
            result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            log.info("操作日志 - 模块：{}，操作：{}，用户：{}，方法：{}，耗时：{}ms",
                    operationLog.module(),
                    operationLog.operation(),
                    username,
                    joinPoint.getSignature().getName(),
                    duration);

            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;

            log.error("操作日志 - 模块：{}，操作：{}，用户：{}，方法：{}，耗时：{}ms，异常：{}",
                    operationLog.module(),
                    operationLog.operation(),
                    username,
                    joinPoint.getSignature().getName(),
                    duration,
                    e.getMessage());

            throw e;
        }
    }
}
