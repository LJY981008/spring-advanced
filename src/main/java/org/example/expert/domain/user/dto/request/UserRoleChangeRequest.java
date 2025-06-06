package org.example.expert.domain.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleChangeRequest {

    @NotNull(message = "변경할 역할이 비어있습니다.")
    private String role;
}
