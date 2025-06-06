package org.example.expert.client;

import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @InjectMocks
    private WeatherClient weatherClient;

    private String today;

    @BeforeEach
    void setUp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        today = LocalDate.now().format(formatter);
        
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        weatherClient = new WeatherClient(restTemplateBuilder);
    }

    @Test
    @DisplayName("오늘 날씨 데이터를 성공적으로 조회한다")
    void getTodayWeather_Success() {
        // given
        WeatherDto[] weatherData = new WeatherDto[]{
            new WeatherDto(today, "맑음"),
            new WeatherDto("01-01", "흐림")
        };
        ResponseEntity<WeatherDto[]> response = new ResponseEntity<>(weatherData, HttpStatus.OK);
        when(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class))).thenReturn(response);

        // when
        String result = weatherClient.getTodayWeather();

        // then
        assertThat(result).isEqualTo("맑음");
    }

    @Test
    @DisplayName("API 응답이 실패하면 예외가 발생한다")
    void getTodayWeather_ApiFailure() {
        // given
        ResponseEntity<WeatherDto[]> response = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class))).thenReturn(response);

        // when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
            .isInstanceOf(ServerException.class)
            .hasMessageContaining("날씨 데이터를 가져오는데 실패했습니다");
    }

    @Test
    @DisplayName("날씨 데이터가 없으면 예외가 발생한다")
    void getTodayWeather_NoData() {
        // given
        ResponseEntity<WeatherDto[]> response = new ResponseEntity<>(new WeatherDto[]{}, HttpStatus.OK);
        when(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class))).thenReturn(response);

        // when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
            .isInstanceOf(ServerException.class)
            .hasMessageContaining("날씨 데이터가 없습니다");
    }

    @Test
    @DisplayName("오늘 날짜의 데이터가 없으면 예외가 발생한다")
    void getTodayWeather_NoTodayData() {
        // given
        WeatherDto[] weatherData = new WeatherDto[]{
            new WeatherDto("01-01", "맑음"),
            new WeatherDto("01-02", "흐림")
        };
        ResponseEntity<WeatherDto[]> response = new ResponseEntity<>(weatherData, HttpStatus.OK);
        when(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class))).thenReturn(response);

        // when & then
        assertThatThrownBy(() -> weatherClient.getTodayWeather())
            .isInstanceOf(ServerException.class)
            .hasMessageContaining("오늘에 해당하는 날씨 데이터를 찾을 수 없습니다");
    }
}
