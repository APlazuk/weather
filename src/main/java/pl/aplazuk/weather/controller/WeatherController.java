package pl.aplazuk.weather.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pl.aplazuk.weather.model.*;
import pl.aplazuk.weather.model.location.Location;
import pl.aplazuk.weather.model.rainviewer.RainViewer;
import pl.aplazuk.weather.model.rainviewer.WeatherNowcast;
import pl.aplazuk.weather.model.weatherinfo.Timeseries;
import pl.aplazuk.weather.model.weatherinfo.WeatherInfo;
import pl.aplazuk.weather.service.MapService;
import pl.aplazuk.weather.service.WeatherInfoService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;


@RestController
@CrossOrigin(origins = "${app.url}")
@RequestMapping(value = "/weather")
public class WeatherController {

    private final MapService mapService;
    private final WeatherInfoService weatherInfoService;

    public WeatherController(MapService mapService, WeatherInfoService weatherInfoService) {
        this.mapService = mapService;
        this.weatherInfoService = weatherInfoService;
    }

    @GetMapping(value = "/location/coordinates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Coordinates> getLocationCoordinates(@RequestParam(required = true) String city) {
        Optional<Coordinates> optionalCoordinate = Optional.empty();
        ResponseEntity<Location[]> locationResponseEntity = mapService.getLocation(city);
        if (locationResponseEntity.getStatusCode().is2xxSuccessful() && locationResponseEntity.getBody() != null && locationResponseEntity.hasBody()) {
            optionalCoordinate = Arrays.stream(locationResponseEntity.getBody())
                    .filter(Objects::nonNull)
                    .map(location -> new Coordinates(location.getLat(), location.getLng()))
                    .findFirst();
        }
        return optionalCoordinate.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(locationResponseEntity.getStatusCode()).build());
    }

    @PostMapping(value = "/nowcast", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<WeatherNowcast> getWeatherNowcast(@RequestParam(required = true) String mapZoom, @RequestBody Coordinates defaultCoordinates) {
        ResponseEntity<RainViewer> rainViewerResponseEntity = mapService.getWeatherMap();
        Optional<WeatherNowcast> weatherNowcast = Optional.empty();
        if (rainViewerResponseEntity.getStatusCode().is2xxSuccessful() && rainViewerResponseEntity.getBody() != null) {
            RainViewer weatherMapData = rainViewerResponseEntity.getBody();
            weatherNowcast = Optional.of(new WeatherNowcast(weatherMapData.getHost(), weatherMapData.getRadar().getNowcast()));
            mapService.setRainViewerPathWithCoordinates(weatherNowcast, defaultCoordinates, mapZoom);
        }
        return weatherNowcast.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(rainViewerResponseEntity.getStatusCode()).build());
    }

    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Timeseries> getWeatherInfo(@RequestParam String lat, @RequestParam String lng) {
        ResponseEntity<WeatherInfo> weatherMiniInfo = weatherInfoService.getWeatherMiniInfo(new Coordinates(Double.parseDouble(lat), Double.parseDouble(lng)));
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
}