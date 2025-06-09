package org.example.expert.domain.todo;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;


@ExtendWith(SpringExtension.class)
public class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private TodoService todoService;

    @Test
    @DisplayName("작성한 todo를 저장하고 반환합니다.")
    void saveTodo_success() {
        //given
        AuthUser authUser = new AuthUser(1L, "test@test.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest();
        String weather = "맑음";

        Todo todo = new Todo("testTitle", "testContents", weather, user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        given(weatherClient.getTodayWeather()).willReturn(weather);
        given(todoRepository.save(any(Todo.class))).willReturn(todo);

        //when
        TodoSaveResponse todoSaveResponse = todoService.saveTodo(authUser, todoSaveRequest);

        //then
        assertThat(todoSaveResponse.getId()).isEqualTo(1L);
        assertThat(todoSaveResponse.getTitle()).isEqualTo("testTitle");
        assertThat(todoSaveResponse.getContents()).isEqualTo("testContents");
        assertThat(todoSaveResponse.getWeather()).isEqualTo(weather);
        assertThat(todoSaveResponse.getUser().getId()).isEqualTo(user.getId());
        assertThat(todoSaveResponse.getUser().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("페이지네이션한 Todo들을 조회하고 반환합니다.")
    void getTodos_success() {
        //given
        int page = 1;
        int size = 10;
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "email", "test@test.com");

        Todo todo = new Todo("testTitle", "testContents", "맑음", user);
        ReflectionTestUtils.setField(todo, "id", 1L);
        ReflectionTestUtils.setField(todo, "createdAt", now);
        ReflectionTestUtils.setField(todo, "modifiedAt", now);

        List<Todo> todoList = new ArrayList<>(List.of(todo, todo, todo));
        Page<Todo> todos = new PageImpl<Todo>(todoList, PageRequest.of(page, size), size);
        given(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class))).willReturn(todos);

        //when
        Page<TodoResponse> resultTodos = todoService.getTodos(page, size);

        //then
        resultTodos.getContent().forEach(todoResponse -> {
            assertThat(todoResponse.getId()).isEqualTo(1L);
            assertThat(todoResponse.getTitle()).isEqualTo("testTitle");
            assertThat(todoResponse.getContents()).isEqualTo("testContents");
            assertThat(todoResponse.getWeather()).isEqualTo("맑음");
            assertThat(todoResponse.getUser().getId()).isEqualTo(1L);
            assertThat(todoResponse.getUser().getEmail()).isEqualTo("test@test.com");
            assertThat(todoResponse.getCreatedAt()).isEqualTo(now);
            assertThat(todoResponse.getModifiedAt()).isEqualTo(now);
        });
    }

    @Test
    @DisplayName("todo를 단건 조회해서 반환합니다.")
    void getTodo_success() {
        //given
        long todoId = 1L;
        LocalDateTime now = LocalDateTime.now();

        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "email", "test@test.com");

        Todo todo = new Todo("testTitle", "testContents", "맑음", user);
        ReflectionTestUtils.setField(todo, "id", todoId);
        ReflectionTestUtils.setField(todo, "createdAt", now);
        ReflectionTestUtils.setField(todo, "modifiedAt", now);

        given(todoRepository.findTodoById(anyLong())).willReturn(Optional.of(todo));

        //when
        TodoResponse todoResponse = todoService.getTodo(todoId);

        //then
        assertThat(todoResponse.getId()).isEqualTo(1L);
        assertThat(todoResponse.getTitle()).isEqualTo("testTitle");
        assertThat(todoResponse.getContents()).isEqualTo("testContents");
        assertThat(todoResponse.getWeather()).isEqualTo("맑음");
        assertThat(todoResponse.getUser().getId()).isEqualTo(1L);
        assertThat(todoResponse.getUser().getEmail()).isEqualTo("test@test.com");
        assertThat(todoResponse.getCreatedAt()).isEqualTo(now);
        assertThat(todoResponse.getModifiedAt()).isEqualTo(now);
    }
}
