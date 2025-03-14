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
import pl.aplazuk.weather.model.location.Location;
import pl.aplazuk.weather.model.rainviewer.Nowcast;
import pl.aplazuk.weather.model.rainviewer.Radar;
import pl.aplazuk.weather.model.rainviewer.RainViewer;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;


class MapServiceTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private MapService mapService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapService = new MapService(restClientBuilder);
        when(restClientBuilder.build()).thenReturn(restClient);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.accept(eq(APPLICATION_JSON)))
                .thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

    }

    @Test
    void shouldReturnLocationArrayWhenCityIsProvided() {
        //given
        Location location = new Location();
        location.setPlaceId(167984640);
        location.setLat(51.1089776);
        location.setLng(17.0326689);
        location.setName("Wrocław");
        location.setDisplayName("Wrocław, województwo dolnośląskie, Polska");
        Location[] mockResponse = {location};
        when(requestHeadersUriSpec.uri(
                any(URI.class)
        )).thenReturn(requestHeadersUriSpec);
        when(responseSpec.toEntity(Location[].class)).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));
        //when

        ResponseEntity<Location[]> result = mapService.getLocation("Wrocław");
        //then

        Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(result.getBody()).isEqualTo(mockResponse);
        Assertions.assertThat(result.getBody()[0].getLat()).isEqualTo(51.1089776);
        Assertions.assertThat(result.getBody()[0].getLng()).isEqualTo(17.0326689);
        Assertions.assertThat(result.getBody()[0].getName()).isEqualTo("Wrocław");
    }

    @Test
    void shouldReturn4xxStatusWhenNoCityIsProvided() {
        //given
        when(requestHeadersUriSpec.uri(
                any(URI.class)
        )).thenReturn(requestHeadersUriSpec);
        when(responseSpec.toEntity(Location[].class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        //when
        HttpClientErrorException result = assertThrows(HttpClientErrorException.class, () -> mapService.getLocation(""));

        //then
        Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(result.getStatusText()).isEqualTo("BAD_REQUEST");
    }

    @Test
    void shouldReturnWeatherMapDetails() {
        //given
        RainViewer mockResponse = new RainViewer();
        Radar radar = new Radar();
        Nowcast nowcast = new Nowcast();
        nowcast.setPath("/v2/radar/nowcast_15b401042a27");
        nowcast.setTime(1741804200);
        List<Nowcast> nowcastList = List.of(nowcast);
        radar.setNowcast(nowcastList);
        mockResponse.setRadar(radar);
        when(requestHeadersUriSpec.uri(
                anyString(), any(Object[].class)
        )).thenReturn(requestHeadersUriSpec);
        when(responseSpec.toEntity(RainViewer.class)).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        //when
        ResponseEntity<RainViewer> result = mapService.getWeatherMap();

        //then
        Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(result.getBody()).isNotNull().isInstanceOf(RainViewer.class);
        Assertions.assertThat(result.getBody().getRadar().getNowcast().get(0).getPath()).isEqualTo("/v2/radar/nowcast_15b401042a27");
        Assertions.assertThat(result.getBody().getRadar().getNowcast().get(0).getTime()).isEqualTo(1741804200);

    }

    @Test
    void shouldReturn4xxStatusWhenWeatherMapDetailsRetrieved() {
        //given
        when(requestHeadersUriSpec.uri(
                anyString(), any(Object[].class)
        )).thenReturn(requestHeadersUriSpec);
        when(responseSpec.toEntity(RainViewer.class)).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        //when
        HttpClientErrorException result = assertThrows(HttpClientErrorException.class, () -> mapService.getWeatherMap());

        //then
        Assertions.assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(result.getStatusText()).isEqualTo("BAD_REQUEST");
    }


}