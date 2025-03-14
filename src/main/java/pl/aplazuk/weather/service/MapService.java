package pl.aplazuk.weather.service;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import pl.aplazuk.weather.model.Coordinates;
import pl.aplazuk.weather.model.location.Location;
import pl.aplazuk.weather.model.rainviewer.RainViewer;
import pl.aplazuk.weather.model.rainviewer.WeatherNowcast;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
public class MapService {

    private static final String LOCATION_COORDINATES_URL = "https://nominatim.openstreetmap.org/search";
    private static final String RADAR_VIEWER_URL = "https://api.rainviewer.com/public/weather-maps.json";
    private final RestClient.Builder restClient;

    public MapService(RestClient.Builder restClient) {
        this.restClient = restClient;
    }


    public ResponseEntity<Location[]> getLocation(String city) {
        return restClient.build().get()
                .uri(getLocationCoordinatesUri(city))
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    ResponseEntity.status(response.getStatusCode()).body(response.getStatusText());
                    throw new HttpClientErrorException(response.getStatusCode());
                })
                .toEntity(Location[].class);
    }

    public ResponseEntity<RainViewer> getWeatherMap() {
        return restClient.build().get()
                .uri(RADAR_VIEWER_URL)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                    ResponseEntity.status(response.getStatusCode()).body(response.getStatusText());
                    throw new HttpClientErrorException(response.getStatusCode());
                }))
                .toEntity(RainViewer.class);
    }

    public void setRainViewerPathWithCoordinates(Optional<WeatherNowcast> weatherNowcast, Coordinates defaultCoordinates,
                                                 String mapZoom) {
        if (weatherNowcast.isPresent() && (defaultCoordinates != null &&
                (defaultCoordinates.getLat() != null || defaultCoordinates.getLng() != null))) {
            //based on API documentation pattern is /v2/radar/nowcast_4a396bdcbe1a/{size}/{z}/{lat}/{lon}/{color}/{options}.png
            weatherNowcast.get().getNowcast().forEach(nowcast -> {
                String relativePath = String.format(Locale.US, "/512/%1$s/%2$f/%3$f/2/1_1.png", mapZoom, defaultCoordinates.getLat(), defaultCoordinates.getLng());
                nowcast.setPath(String.join("", nowcast.getPath(), relativePath));
            });
        }
    }

    private static URI getLocationCoordinatesUri(String city) {
        return UriComponentsBuilder.fromHttpUrl(LOCATION_COORDINATES_URL)
                .queryParam("city", city)
                .queryParam("format", "jsonv2")
                .queryParam("addressdetails", 1)
                .queryParam("limit", 1)
                .build()
                .toUri();
    }
}
