package org.example.expert.config;

import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("InvalidRequestException 발생 시 400 상태코드와 에러 메시지를 반환한다")
    void handleInvalidRequestException() {
        // given
        String errorMessage = "잘못된 요청입니다";
        InvalidRequestException exception = new InvalidRequestException(errorMessage);

        // when
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleInvalidRequestException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.BAD_REQUEST.name());
        assertThat(response.getBody().get("code")).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().get("message")).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("AuthException 발생 시 401 상태코드와 에러 메시지를 반환한다")
    void handleAuthException() {
        // given
        String errorMessage = "인증에 실패했습니다";
        AuthException exception = new AuthException(errorMessage);

        // when
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleAuthException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.UNAUTHORIZED.name());
        assertThat(response.getBody().get("code")).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getBody().get("message")).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("ServerException 발생 시 500 상태코드와 에러 메시지를 반환한다")
    void handleServerException() {
        // given
        String errorMessage = "서버 오류가 발생했습니다";
        ServerException exception = new ServerException(errorMessage);

        // when
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleServerException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.name());
        assertThat(response.getBody().get("code")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getBody().get("message")).isEqualTo(errorMessage);
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 발생 시 400 상태코드와 기본 에러 메시지를 반환한다")
    void handleMethodArgumentNotValidException_without_field_error() {
        // given
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(exception.getBindingResult().getFieldError()).thenReturn(null);

        // when
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleMethodArgumentNotValidException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.BAD_REQUEST.name());
        assertThat(response.getBody().get("code")).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().get("message")).isEqualTo("Bad Request");
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 발생 시 400 상태코드와 필드 에러 메시지를 반환한다")
    void handleMethodArgumentNotValidException_with_field_error() {
        // given
        String errorMessage = "이메일은 필수 입력값입니다";
        FieldError fieldError = new FieldError("object", "field", errorMessage);
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(exception.getBindingResult().getFieldError()).thenReturn(fieldError);


        // when
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleMethodArgumentNotValidException(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.BAD_REQUEST.name());
        assertThat(response.getBody().get("code")).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().get("message")).isEqualTo(errorMessage);
    }
}
