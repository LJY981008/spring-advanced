package org.example.expert.domain.auth;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.auth.service.AuthService;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    private final String testEmail = "test@test.com";
    private final String testPW = "Aa123456";
    private final String testToken = "test.jwt.token";

    @Test
    @DisplayName("회원가입 성공 시 생성된 토큰을 반환합니다.")
    void signup_success() {
        //given
        SignupRequest signupRequest = new SignupRequest(
                testEmail,
                testPW,
                UserRole.USER.name()
        );

        User savedUser = new User(
                signupRequest.getEmail(),
                testPW,
                UserRole.USER
        );

        // when
        when(passwordEncoder.encode(anyString())).thenReturn(testPW);
        when(jwtUtil.createToken(any(), any(), any())).thenReturn(testToken);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        SignupResponse signup = authService.signup(signupRequest);

        // then
        assertThat(signup.getBearerToken()).isEqualTo(testToken);
        verify(userRepository).existsByEmail(signupRequest.getEmail());
        verify(passwordEncoder).encode(signupRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).createToken(savedUser.getId(), savedUser.getEmail(), UserRole.USER);
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 가입을 시도하면 예외가 발생합니다.")
    void signup_fail() {
        //given
        SignupRequest signupRequest = new SignupRequest(testEmail, testPW, UserRole.USER.name());
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        //when & then
        assertThatThrownBy(() -> authService.signup(signupRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }

    @Test
    @DisplayName("로그인 성공 시 생성된 토큰을 반환헙니다")
    void signin_success() {
        //given
        SigninRequest signinRequest = new SigninRequest(testEmail, testPW);
        User user = new User(
                signinRequest.getEmail(),
                testPW,
                UserRole.USER
        );
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.createToken(any(), any(), any())).thenReturn(testToken);

        //when
        SigninResponse signin = authService.signin(signinRequest);

        //then
        assertThat(signin.getBearerToken()).isEqualTo(testToken);
        verify(userRepository).findByEmail(testEmail);
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(jwtUtil).createToken(any(), any(), any());
    }

    @Test
    @DisplayName("가입되지 않은 로그인 시도 시 예외가 발생합니다.")
    void signin_not_email() {
        //given
        SigninRequest signinRequest = new SigninRequest(testEmail, testPW);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> authService.signin(signinRequest))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("가입되지 않은 유저입니다.");
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않을 시 예외가 발생합니다.")
    void signin_invalid_password() {
        SigninRequest signinRequest = new SigninRequest(testEmail, testPW);
        User user = new User(
                signinRequest.getEmail(),
                testPW,
                UserRole.USER
        );
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        //when & then
        assertThatThrownBy(() -> authService.signin(signinRequest))
                .isInstanceOf(AuthException.class)
                .hasMessage("잘못된 비밀번호입니다.");
    }
}
