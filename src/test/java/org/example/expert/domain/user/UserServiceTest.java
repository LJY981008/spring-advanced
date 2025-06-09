package org.example.expert.domain.user;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("유저를 조회합니다.")
    void getUser_() {
        //given
        User user = new User("test@test.com", "Aa123456", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        //when
        UserResponse resultUser = userService.getUser(anyLong());

        //then
        assertThat(resultUser.getId()).isEqualTo(1L);
        assertThat(resultUser.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("유저의 비밀번호를 변경합니다.")
    void changePassword_success() {
        //given
        long userId = 1L;
        UserChangePasswordRequest request = new UserChangePasswordRequest("Test1234", "Test3456");

        User user = mock(User.class);

        given(passwordEncoder.matches(request.getOldPassword(), request.getNewPassword())).willReturn(false);
        given(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).willReturn(true);
        given(passwordEncoder.encode(request.getNewPassword())).willReturn(request.getNewPassword());
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        //when
        userService.changePassword(userId, request);

        //then
        verify(user).changePassword(anyString());
    }

    @Test
    @DisplayName("비밀번호 수정 시 새 비밀번호와 기존 비밀번호가 같으면 예외가 발생합니다.")
    void changePassword_old_new_equal() {
        //given
        long userId = 1L;
        UserChangePasswordRequest request = mock(UserChangePasswordRequest.class);

        given(passwordEncoder.matches(request.getOldPassword(), request.getNewPassword())).willReturn(true);

        //when & then
        assertThatThrownBy(() -> userService.changePassword(userId, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
    }

    @Test
    @DisplayName("비밀번호 수정 시 원래 입력한 비밀번호가 틀리면 예외가 발생합니다.")
    void changePassword_invalid_password() {
        //given
        long userId = 1L;
        UserChangePasswordRequest request = mock(UserChangePasswordRequest.class);
        User user = mock(User.class);

        given(passwordEncoder.matches(request.getOldPassword(), request.getNewPassword())).willReturn(false);
        given(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).willReturn(false);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        //when
        assertThatThrownBy(() -> userService.changePassword(userId, request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("잘못된 비밀번호입니다.");

    }
}
