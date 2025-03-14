package pl.aplazuk.weather.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import pl.aplazuk.weather.model.Coordinates;
import pl.aplazuk.weather.model.weatherinfo.Geometry;
import pl.aplazuk.weather.model.weatherinfo.WeatherInfo;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

class WeatherInfoServiceTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private WeatherInfoService weatherInfoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        weatherInfoService = new WeatherInfoService(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.accept(eq(APPLICATION_JSON)))
                .thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

    }

    @Test
    void shouldReturnWeatherInfoWhenCoordinatesAreProvided() {
        //given
        WeatherInfo mockResponse = new WeatherInfo();
        Geometry geometry = new Geometry();
        geometry.setCoordinates(List.of(51.1089776, 17.0326689));
        mockResponse.setGeometry(geometry);

        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(responseSpec.toEntity(WeatherInfo.class)).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        //when
        ResponseEntity<WeatherInfo> result = weatherInfoService.getWeatherMiniInfo(new Coordinates(51.1089776, 17.0326689));

        //then
        Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(result.getBody()).isEqualTo(mockResponse);
        Assertions.assertThat(result.getBody().getGeometry().getCoordinates().get(0)).isEqualTo(51.1089776);
        Assertions.assertThat(result.getBody().getGeometry().getCoordinates().get(1)).isEqualTo(17.0326689);
    }

    @Test
    void shouldReturn4xxWhenCoordinatesAreNotGiven() {
        //given
        when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersUriSpec);
        when(responseSpec.toEntity(WeatherInfo.class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        //when
        HttpClientErrorException result = assertThrows(HttpClientErrorException.class,
                () -> weatherInfoService.getWeatherMiniInfo(new Coordinates(null, null)));

        //then
        Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(result.getStatusText()).isEqualTo("BAD_REQUEST");
    }
}