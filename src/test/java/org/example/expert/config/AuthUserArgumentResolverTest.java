package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUserArgumentResolverTest {

    @InjectMocks
    private AuthUserArgumentResolver authUserArgumentResolver;

    @Mock
    private NativeWebRequest webRequest;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
    }

    @Test
    @DisplayName("Auth 어노테이션과 AuthUser 타입이 함께 사용된 경우 true를 반환한다")
    void supportsParameter_success() throws NoSuchMethodException {
        // given
        MethodParameter methodParameter = new MethodParameter(
                TestController.class.getMethod("testMethod", AuthUser.class), 0);

        // when
        boolean result = authUserArgumentResolver.supportsParameter(methodParameter);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Auth 어노테이션만 있는 경우 예외가 발생한다")
    void supportsParameter_fail_when_only_auth_annotation() throws NoSuchMethodException {
        // given
        MethodParameter methodParameter = new MethodParameter(
                TestController.class.getMethod("testMethodWithOnlyAuth", String.class), 0);

        // when & then
        assertThatThrownBy(() -> authUserArgumentResolver.supportsParameter(methodParameter))
                .isInstanceOf(AuthException.class)
                .hasMessage("@Auth와 AuthUser 타입은 함께 사용되어야 합니다.");
    }

    @Test
    @DisplayName("AuthUser 타입만 있는 경우 예외가 발생한다")
    void supportsParameter_fail_when_only_auth_user_type() throws NoSuchMethodException {
        // given
        MethodParameter methodParameter = new MethodParameter(
                TestController.class.getMethod("testMethodWithOnlyAuthUser", AuthUser.class), 0);

        // when & then
        assertThatThrownBy(() -> authUserArgumentResolver.supportsParameter(methodParameter))
                .isInstanceOf(AuthException.class)
                .hasMessage("@Auth와 AuthUser 타입은 함께 사용되어야 합니다.");
    }

    @Test
    @DisplayName("AuthUser 객체를 정상적으로 반환한다")
    void resolveArgument_success() {
        // given
        request.setAttribute("userId", 1L);
        request.setAttribute("email", "test@test.com");
        request.setAttribute("userRole", UserRole.ADMIN.name());
        when(webRequest.getNativeRequest()).thenReturn(request);

        // when
        AuthUser result = (AuthUser) authUserArgumentResolver.resolveArgument(
                null, null, webRequest, null);

        // then
        Assertions.assertNotNull(result);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@test.com");
        assertThat(result.getUserRole()).isEqualTo(UserRole.ADMIN);
    }

    private static class TestController {
        @GetMapping("/test")
        public void testMethod(@Auth AuthUser authUser) {
        }

        @GetMapping("/test-only-auth")
        public void testMethodWithOnlyAuth(@Auth String param) {
        }

        @GetMapping("/test-only-auth-user")
        public void testMethodWithOnlyAuthUser(AuthUser authUser) {
        }
    }
}
