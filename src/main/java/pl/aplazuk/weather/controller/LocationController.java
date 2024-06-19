package pl.aplazuk.weather.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import pl.aplazuk.weather.model.*;

import java.net.URI;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RestController
@CrossOrigin(origins = "http://localhost:4200/")
public class LocationController {

    private Coordinates coordinates;

    private static final String LOCATION_COORDINATES_URL = "https://nominatim.openstreetmap.org/search";
    private static final String RADAR_VIEWER_URL = "https://api.rainviewer.com/public/weather-maps.json";
    private final RestClient restClient = RestClient.create();

    public LocationController() {
    }

    protected ResponseEntity<Location[]> getLocation(String city) {
        return restClient.get()
                .uri(getLocationCoordinatesUri(city))
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    ResponseEntity.status(response.getStatusCode()).body(response.getStatusText());
                })
                .toEntity(Location[].class);
    }

    protected ResponseEntity<RainViewer> getWeatherMap() {
        return restClient.get()
                .uri(RADAR_VIEWER_URL)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                    ResponseEntity.status(response.getStatusCode()).body(response.getStatusText());
                }))
                .toEntity(RainViewer.class);
    }

    @GetMapping(value = "/location/coordinates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Coordinates> getLocationCoordinates(@RequestParam(required = true) String city) {
        Optional<Coordinates> optionalCoordinate = Optional.empty();
        ResponseEntity<Location[]> locationResponseEntity = getLocation(city);
        if (locationResponseEntity.getStatusCode().is2xxSuccessful() && locationResponseEntity.getBody() != null && locationResponseEntity.hasBody()) {
            optionalCoordinate = Arrays.stream(locationResponseEntity.getBody())
                    .filter(Objects::nonNull)
                    .map(location -> coordinates = new Coordinates(location.getLat(), location.getLng()))
                    .findFirst();
        }
        return optionalCoordinate.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(locationResponseEntity.getStatusCode()).body(null));
    }

    @PostMapping(value = "/weather-nowcast", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WeatherNowcast> getWeatherNowcast(@RequestParam(required = true) String mapZoom, @RequestBody Coordinates defaultCoordinates) {
        ResponseEntity<RainViewer> rainViewerResponseEntity = getWeatherMap();
        Optional<WeatherNowcast> weatherNowcast = Optional.empty();
        if (rainViewerResponseEntity.getStatusCode().is2xxSuccessful() && rainViewerResponseEntity.getBody() != null) {
            RainViewer weatherMapData = rainViewerResponseEntity.getBody();
            weatherNowcast = Optional.of(new WeatherNowcast(weatherMapData.getHost(), weatherMapData.getRadar().getNowcast()));
            setRainViewerPathWithCoordinates(weatherNowcast, defaultCoordinates, mapZoom);
        }
        return weatherNowcast.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(rainViewerResponseEntity.getStatusCode()).body(null));
    }

    private void setRainViewerPathWithCoordinates(Optional<WeatherNowcast> weatherNowcast, Coordinates defaultCoordinates, String mapZoom) {
        if ((coordinates != null && weatherNowcast.isPresent()) || defaultCoordinates != null) {
            //based on API documentation pattern is /v2/radar/nowcast_4a396bdcbe1a/{size}/{z}/{lat}/{lon}/{color}/{options}.png
            weatherNowcast.get().getNowcast().forEach(nowcast -> {
                if (defaultCoordinates != null && (defaultCoordinates.getLat() != null || defaultCoordinates.getLng() != null)) {
                    coordinates = defaultCoordinates;
                }
                String relativePath = String.format("/512/{z}/%1$f/%2$f/4/1_0.png", coordinates.getLat(), coordinates.getLng());
                nowcast.setPath(String.join("", nowcast.getPath(), relativePath));
            });
        }
    }

    private static URI getLocationCoordinatesUri(String city) {
        return UriComponentsBuilder.fromHttpUrl(LOCATION_COORDINATES_URL)
                .queryParam("city", city)
                .queryParam("addressdetails", 1)
                .queryParam("format", "jsonv2")
                .queryParam("limit", 1)
                .build()
                .toUri();
    }
}