package org.example.expert.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class JwtFilterTest {

    @Mock
    FilterChain chain;

    MockHttpServletRequest servletRequest = new MockHttpServletRequest();
    MockHttpServletResponse servletResponse = new MockHttpServletResponse();

    @Spy
    JwtUtil jwtUtil = new JwtUtil();

    JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        jwtUtil.init();
        jwtFilter = new JwtFilter(jwtUtil);
    }

    @Test
    @DisplayName("인증이 필요한 경우 토큰을 검증합니다.")
    void doFilter_token_valid_success() throws Exception {
        //given
        String token = jwtUtil.createToken(1L, "test@test.com", UserRole.USER);
        servletRequest.setRequestURI("/delete");
        servletRequest.addHeader("Authorization", token);

        //when
        jwtFilter.doFilter(servletRequest, servletResponse, chain);

        //then
        verify(chain).doFilter(servletRequest, servletResponse);
        assertThat(servletRequest.getAttribute("userId")).isEqualTo(1L);
        assertThat(servletRequest.getAttribute("email")).isEqualTo("test@test.com");
        assertThat(servletRequest.getAttribute("userRole")).isEqualTo(UserRole.USER.name());
    }

    @Test
    @DisplayName("인증이 필요없는 경우 다음 필터로 이동합니다.")
    void doFilter_auth_return_next_filter() throws Exception {
        //given
        servletRequest.setRequestURI("/auth");

        //when
        jwtFilter.doFilter(servletRequest, servletResponse, chain);

        //then
        verify(chain).doFilter(servletRequest, servletResponse);
        verifyNoMoreInteractions(chain);
    }

    @Test
    @DisplayName("토큰이 없는 경우 예외 상태코드와 메시지를 응답합니다.")
    void doFilter_not_token_exception() throws Exception {
        //given
        servletRequest.setRequestURI("/delete");

        //when
        jwtFilter.doFilter(servletRequest, servletResponse, chain);

        //then
        assertThat(servletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
        assertThat(servletResponse.getErrorMessage()).isEqualTo("JWT 토큰이 필요합니다.");
    }

    @Test
    @DisplayName("토큰에 정보가 없는 경우 예외 상태코드와 메시지를 응답합니다.")
    void doFilter_not_claims() throws Exception {
        //given
        String token = jwtUtil.createToken(1L, "test@test.com", UserRole.USER);
        FilterChain chain = mock(FilterChain.class);
        servletRequest.setRequestURI("/delete");
        servletRequest.addHeader("Authorization", token);
        doReturn(null).when(jwtUtil).extractClaims(anyString());

        //when
        jwtFilter.doFilter(servletRequest, servletResponse, chain);

        //then
        assertThat(servletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
        assertThat(servletResponse.getErrorMessage()).isEqualTo("잘못된 JWT 토큰입니다.");
    }

    @Test
    @DisplayName("관리자 권한인 상태에서 admin 접근 시 통과합니다.")
    void doFilter_permission_approach()throws Exception {
        //given
        String token = jwtUtil.createToken(1L, "test@test.com", UserRole.ADMIN);
        servletRequest.setRequestURI("/admin");
        servletRequest.addHeader("Authorization", token);

        //when
        jwtFilter.doFilter(servletRequest, servletResponse, chain);

        //then
        verify(chain).doFilter(servletRequest, servletResponse);
        assertThat(servletRequest.getAttribute("userId")).isEqualTo(1L);
        assertThat(servletRequest.getAttribute("email")).isEqualTo("test@test.com");
        assertThat(servletRequest.getAttribute("userRole")).isEqualTo(UserRole.ADMIN.name());

    }
    @Test
    @DisplayName("관리자 권한이 없는 상태에서 admin에 접근 시 예외 상태코드와 메시지를 응답합니다.")
    void doFilter_not_permission_approach() throws Exception {
        //given
        String token = jwtUtil.createToken(1L, "test@test.com", UserRole.USER);
        servletRequest.setRequestURI("/admin");
        servletRequest.addHeader("Authorization", token);

        //when
        jwtFilter.doFilter(servletRequest, servletResponse, chain);

        //then
        assertThat(servletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(servletResponse.getErrorMessage()).isEqualTo("관리자 권한이 없습니다.");
    }

    @Test
    @DisplayName("Bearer 형식이 아닌 토큰은 예외를 반환합니다.")
    void doFilter_invalid_token_format() throws Exception {
        //given
        String token = "InvalidToken";
        FilterChain chain = mock(FilterChain.class);
        servletRequest.setRequestURI("/delete");
        servletRequest.addHeader("Authorization", token);

        //when & then
        assertThatThrownBy(() -> {
            jwtFilter.doFilter(servletRequest, servletResponse, chain);
        })
                .isInstanceOf(ServerException.class)
                .hasMessage("Not Found Token");
    }

    @Test
    @DisplayName("잘못된 형식의 JWT 토큰은 예외 상태코드와 메시지를 응답합니다.")
    void doFilter_malformed_token() throws Exception {
        //given
        String malformedToken = "invalid.token.format";
        FilterChain chain = mock(FilterChain.class);
        servletRequest.setRequestURI("/delete");
        servletRequest.addHeader("Authorization", "Bearer " + malformedToken);

        //when
        jwtFilter.doFilter(servletRequest, servletResponse, chain);

        //then
        assertThat(servletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(servletResponse.getErrorMessage()).isEqualTo("유효하지 않는 JWT 서명입니다.");
    }

    @Test
    @DisplayName("유효하지 않은 JWT 서명은 예외 상태코드와 메시지를 응답합니다.")
    void doFilter_invalid_signature() throws Exception {
        //given
        String token = jwtUtil.createToken(1L, "test@test.com", UserRole.USER);
        servletRequest.setRequestURI("/delete");
        servletRequest.addHeader("Authorization", "Bearer " + token);

        // SecurityException 발생시키기
        doThrow(new SecurityException("유효하지 않는 JWT 서명입니다."))
            .when(jwtUtil).extractClaims(anyString());

        //when
        jwtFilter.doFilter(servletRequest, servletResponse, chain);

        //then
        assertThat(servletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(servletResponse.getErrorMessage()).isEqualTo("유효하지 않는 JWT 서명입니다.");
    }

    @Test
    @DisplayName("만료된 JWT 토큰은 예외 상태코드와 메시지를 응답합니다.")
    void doFilter_expired_token() throws Exception {
        //given
        String token = jwtUtil.createToken(1L, "test@test.com", UserRole.USER);
        servletRequest.setRequestURI("/delete");
        servletRequest.addHeader("Authorization", "Bearer " + token);

        // ExpiredJwtException 발생시키기
        doThrow(new ExpiredJwtException(null, null, "만료된 JWT 토큰입니다."))
            .when(jwtUtil).extractClaims(anyString());

        //when
        jwtFilter.doFilter(servletRequest, servletResponse, chain);

        //then
        assertThat(servletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
        assertThat(servletResponse.getErrorMessage()).isEqualTo("만료된 JWT 토큰입니다.");
    }

    @Test
    @DisplayName("지원하지 않는 JWT 토큰은 예외 상태코드와 메시지를 응답합니다.")
    void doFilter_unsupported_token() throws Exception {
        //given
        String token = jwtUtil.createToken(1L, "test@test.com", UserRole.USER);
        servletRequest.setRequestURI("/delete");
        servletRequest.addHeader("Authorization", "Bearer " + token);

        // UnsupportedJwtException 발생시키기
        doThrow(new UnsupportedJwtException("지원되지 않는 JWT 토큰입니다."))
            .when(jwtUtil).extractClaims(anyString());

        //when
        jwtFilter.doFilter(servletRequest, servletResponse, chain);

        //then
        assertThat(servletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
        assertThat(servletResponse.getErrorMessage()).isEqualTo("지원되지 않는 JWT 토큰입니다.");
    }

    @Test
    @DisplayName("기타 예외 발생 시 예외 상태코드와 메시지를 응답합니다.")
    void doFilter_general_exception() throws Exception {
        //given
        String token = jwtUtil.createToken(1L, "test@test.com", UserRole.USER);
        servletRequest.setRequestURI("/delete");
        servletRequest.addHeader("Authorization", "Bearer " + token);

        // 일반 Exception 발생시키기
        doThrow(new RuntimeException("유효하지 않는 JWT 토큰입니다."))
            .when(jwtUtil).extractClaims(anyString());

        //when
        jwtFilter.doFilter(servletRequest, servletResponse, chain);

        //then
        assertThat(servletResponse.getStatus()).isEqualTo(HttpServletResponse.SC_BAD_REQUEST);
        assertThat(servletResponse.getErrorMessage()).isEqualTo("유효하지 않는 JWT 토큰입니다.");
    }
}
