package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminLoggingAspectTest {

    @InjectMocks
    private AdminLoggingAspect adminLoggingAspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("관리자 권한으로 API 요청 시 정상 동작한다")
    void adminLogger_success() throws Throwable {
        // given
        request.addHeader("User-Role", UserRole.ADMIN.name());
        request.addHeader("User-Id", "1");
        request.setRequestURI("/admin/test");

        Object testResult = "test";
        when(joinPoint.proceed()).thenReturn(testResult);
        when(joinPoint.getArgs()).thenReturn(new Object[]{testResult});

        // when
        Object result = adminLoggingAspect.adminLogger(joinPoint);

        // then
        assertThat(result).isEqualTo(testResult);
        verify(joinPoint).proceed();
    }

    @Test
    @DisplayName("일반 사용자가 API 요청 시 예외가 발생한다")
    void adminLogger_fail_when_not_admin() {
        // given
        request.addHeader("User-Role", UserRole.USER.name());
        request.addHeader("User-Id", "1");
        request.setRequestURI("/admin/test");

        // when & then
        assertThatThrownBy(() -> adminLoggingAspect.adminLogger(joinPoint))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("관리자 권한이 없습니다");
    }

    @Test
    @DisplayName("User-Role 헤더가 없는 경우 예외가 발생한다")
    void adminLogger_fail_when_no_user_role() {
        // given
        request.addHeader("User-Id", "1");
        request.setRequestURI("/admin/test");

        // when & then
        assertThatThrownBy(() -> adminLoggingAspect.adminLogger(joinPoint))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("관리자 권한이 없습니다");
    }

    @Test
    @DisplayName("요청 처리 중 예외가 발생하면 로깅 후 예외를 다시 던진다")
    void adminLogger_fail_when_exception_occurs() throws Throwable {
        // given
        request.addHeader("User-Role", UserRole.ADMIN.name());
        request.addHeader("User-Id", "1");
        request.setRequestURI("/admin/test");

        RuntimeException expectedException = new RuntimeException("테스트 예외");
        when(joinPoint.proceed()).thenThrow(expectedException);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"test"});

        // when & then
        assertThatThrownBy(() -> adminLoggingAspect.adminLogger(joinPoint))
                .isEqualTo(expectedException);
    }

    @Test
    @DisplayName("RequestContextHolder에 속성이 없는 경우 예외가 발생한다")
    void adminLogger_fail_when_no_attributes() {
        // given
        RequestContextHolder.resetRequestAttributes();

        // when & then
        assertThatThrownBy(() -> adminLoggingAspect.adminLogger(joinPoint))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("available이 존재하지 않습니다");
    }
}
