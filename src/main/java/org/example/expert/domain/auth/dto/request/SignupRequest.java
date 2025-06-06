package org.example.expert.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    @NotBlank(message = "이메일이 비어있습니다.")
    @Pattern(regexp = "^[\\w!#$%&'*+/=?`{|}~^.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$", message = "이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "비밀번호가 비어있습니다.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "유저 타입을 정해주세요.")
    private String userRole;
}
