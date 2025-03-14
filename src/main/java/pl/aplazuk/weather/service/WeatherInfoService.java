package pl.aplazuk.weather.service;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import pl.aplazuk.weather.model.Coordinates;
import pl.aplazuk.weather.model.weatherinfo.WeatherInfo;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
public class WeatherInfoService {

    private static final String WEATHER_API_URL = "https://api.met.no/weatherapi/locationforecast/2.0/mini";
    private final RestClient.Builder restClient;

    public WeatherInfoService(RestClient.Builder restClient) {
        this.restClient = restClient;
    }

    public ResponseEntity<WeatherInfo> getWeatherMiniInfo(Coordinates coordinates) {
        return restClient.build().get()
                .uri(getWeatherApiUri(coordinates))
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    ResponseEntity.status(response.getStatusCode()).body(response.getStatusText());
                    throw new HttpClientErrorException(response.getStatusCode());
                })
                .toEntity(WeatherInfo.class);
    }

    private URI getWeatherApiUri(Coordinates coordinates) {
        return UriComponentsBuilder.fromHttpUrl(WEATHER_API_URL)
                .queryParam("lat", coordinates.getLat())
                .queryParam("lon", coordinates.getLng())
                .build()
                .toUri();
    }
}
