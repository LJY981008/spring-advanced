package org.example.expert.domain.todo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TodoSaveRequest {

    @NotBlank(message = "제목이 비어있습니다.")
    private String title;
    @NotBlank(message = "내용이 비어있습니다.")
    private String contents;
}
