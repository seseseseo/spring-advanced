package org.example.expert.client;

import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class WeatherClient {
//외부 날씨 API 호출해서 오늘의 날씨를 가져오는 클라이언트
    private final RestTemplate restTemplate; // HTTP 요청을 보내기 위한 스프링의 기본 HTTP 클라이언트
    // 생성자에게 저걸 보내서 초기화 하고
    public WeatherClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public String getTodayWeather() {
        ResponseEntity<WeatherDto[]> responseEntity =
                restTemplate.getForEntity(buildWeatherApiUri(), WeatherDto[].class);

        /**
         * 이 부분 수정
         * 연속된 if로 바꿈
         */
        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            throw new ServerException("날씨 데이터를 가져오는데 실패했습니다. 상태 코드: " + responseEntity.getStatusCode());
        }

        WeatherDto[] weatherArray = responseEntity.getBody();
        if (weatherArray == null || weatherArray.length == 0) {
            throw new ServerException("날씨 데이터가 없습니다.");
        }


        String today = getCurrentDate();

        for (WeatherDto weatherDto : weatherArray) {
            if (today.equals(weatherDto.getDate())) {
                return weatherDto.getWeather();
            }
        }

        throw new ServerException("오늘에 해당하는 날씨 데이터를 찾을 수 없습니다.");
    }

    private URI buildWeatherApiUri() {
        return UriComponentsBuilder
                .fromUriString("https://f-api.github.io")
                .path("/f-api/weather.json")
                .encode()
                .build()
                .toUri();
    }

    private String getCurrentDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        return LocalDate.now().format(formatter);
    }
}
