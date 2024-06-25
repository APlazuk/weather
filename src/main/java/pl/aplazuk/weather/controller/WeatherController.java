package pl.aplazuk.weather.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import pl.aplazuk.weather.model.*;
import pl.aplazuk.weather.model.location.Location;
import pl.aplazuk.weather.model.rainviewer.RainViewer;
import pl.aplazuk.weather.model.rainviewer.WeatherNowcast;
import pl.aplazuk.weather.model.weatherinfo.Timeseries;
import pl.aplazuk.weather.model.weatherinfo.WeatherInfo;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RestController
@CrossOrigin(origins = "${app.url}")
@RequestMapping(value = "/weather")
public class WeatherController {

    private Coordinates coordinates;

    private static final String LOCATION_COORDINATES_URL = "https://nominatim.openstreetmap.org/search";
    private static final String RADAR_VIEWER_URL = "https://api.rainviewer.com/public/weather-maps.json";
    private static final String WEATHER_API_URL = "https://api.met.no/weatherapi/locationforecast/2.0/mini";
    private final RestClient restClient = RestClient.create();

    public WeatherController() {
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

    protected ResponseEntity<WeatherInfo> getWeatherMiniInfo(Coordinates coordinates) {
        return restClient.get()
                .uri(getWeatherApiUri(coordinates))
                .accept(APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    ResponseEntity.status(response.getStatusCode()).body(response.getStatusText());
                })
                .toEntity(WeatherInfo.class);
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
        return optionalCoordinate.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(locationResponseEntity.getStatusCode()).build());
    }

    @PostMapping(value = "/nowcast", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WeatherNowcast> getWeatherNowcast(@RequestParam(required = true) String mapZoom, @RequestBody Coordinates defaultCoordinates) {
        ResponseEntity<RainViewer> rainViewerResponseEntity = getWeatherMap();
        Optional<WeatherNowcast> weatherNowcast = Optional.empty();
        if (rainViewerResponseEntity.getStatusCode().is2xxSuccessful() && rainViewerResponseEntity.getBody() != null) {
            RainViewer weatherMapData = rainViewerResponseEntity.getBody();
            weatherNowcast = Optional.of(new WeatherNowcast(weatherMapData.getHost(), weatherMapData.getRadar().getNowcast()));
            setRainViewerPathWithCoordinates(weatherNowcast, defaultCoordinates, mapZoom);
        }
        return weatherNowcast.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(rainViewerResponseEntity.getStatusCode()).build());
    }

    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Timeseries> getWeatherInfo(@RequestParam String lat, @RequestParam String lng) {
        ResponseEntity<WeatherInfo> weatherMiniInfo = getWeatherMiniInfo(new Coordinates(Double.parseDouble(lat), Double.parseDouble(lng)));
        Timeseries weatherTimeseries;
        if (weatherMiniInfo.getStatusCode().is2xxSuccessful() && weatherMiniInfo.getBody() != null) {
            weatherTimeseries = weatherMiniInfo.getBody().getProperties().getTimeseries().stream()
                    //want to get possible up to date weather info, past information are not useful from user perspective
                    .filter(timeseries -> timeseries.getTime().isAfter(LocalDateTime.now()))
                    .findFirst()
                    .orElseGet(() -> weatherMiniInfo.getBody().getProperties().getTimeseries().get(0));
            return ResponseEntity.ok(weatherTimeseries);
        }
        return ResponseEntity.status(weatherMiniInfo.getStatusCode()).build();
    }


    private void setRainViewerPathWithCoordinates(Optional<WeatherNowcast> weatherNowcast, Coordinates defaultCoordinates, String mapZoom) {
        if ((coordinates != null && weatherNowcast.isPresent()) || defaultCoordinates != null) {
            //based on API documentation pattern is /v2/radar/nowcast_4a396bdcbe1a/{size}/{z}/{lat}/{lon}/{color}/{options}.png
            weatherNowcast.get().getNowcast().forEach(nowcast -> {
                if (defaultCoordinates != null && (defaultCoordinates.getLat() != null || defaultCoordinates.getLng() != null)) {
                    coordinates = defaultCoordinates;
                }
                String relativePath = String.format(Locale.US, "/512/%1$s/%2$f/%3$f/2/1_1.png", mapZoom, coordinates.getLat(), coordinates.getLng());
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

    private URI getWeatherApiUri(Coordinates coordinates) {
        return UriComponentsBuilder.fromHttpUrl(WEATHER_API_URL)
                .queryParam("lat", coordinates.getLat())
                .queryParam("lon", coordinates.getLng())
                .build()
                .toUri();
    }
}