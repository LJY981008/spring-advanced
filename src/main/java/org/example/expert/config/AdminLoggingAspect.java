package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Aspect
@Component
public class AdminLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(AdminLoggingAspect.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(org.example.expert.domain.common.annotation.AdminLogging)")
    public Object adminLogger(ProceedingJoinPoint joinPoint) throws Throwable {
        Optional<ServletRequestAttributes> attributes = Optional.ofNullable((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        if (attributes.isEmpty()) {
            throw new InvalidRequestException("available이 존재하지 않습니다");
        }

        HttpServletRequest servletRequest = attributes.get().getRequest();

        Optional.ofNullable(servletRequest.getHeader("User-Role"))
                .filter(role -> role.equals(UserRole.ADMIN.name()))
                .orElseThrow(() -> new InvalidRequestException("관리자 권한이 없습니다"));

        String userId = servletRequest.getHeader("User-Id");
        String requestBody = objectMapper.writeValueAsString(joinPoint.getArgs());

        logger.info("Admin API request - Time: {}, URL: {}, UserID: {}, RequestBody: {}",
                System.currentTimeMillis(),
                servletRequest.getRequestURI(),
                userId,
                requestBody);

        try {
            Object result = joinPoint.proceed();

            String responseBody = objectMapper.writeValueAsString(result);
            logger.info("Admin API Response - Time: {}, URL: {}, UserID: {}, ResponseBody: {}",
                    System.currentTimeMillis(),
                    servletRequest.getRequestURI(),
                    userId,
                    responseBody);

            return result;
        } catch (Exception e) {
            logger.error("Admin API Error - Time: {}, URL: {}, UserId: {}, Error: {}",
                    System.currentTimeMillis(),
                    servletRequest.getRequestURI(),
                    userId,
                    e.getMessage());
            throw e;
        }
    }
}
