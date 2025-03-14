package pl.aplazuk.weather.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestClient;
import pl.aplazuk.weather.model.Coordinates;
import pl.aplazuk.weather.model.location.Location;
import pl.aplazuk.weather.model.rainviewer.Nowcast;
import pl.aplazuk.weather.model.rainviewer.Radar;
import pl.aplazuk.weather.model.rainviewer.RainViewer;
import pl.aplazuk.weather.model.rainviewer.WeatherNowcast;
import pl.aplazuk.weather.model.weatherinfo.*;
import pl.aplazuk.weather.service.MapService;
import pl.aplazuk.weather.service.WeatherInfoService;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@SpringBootTest(properties = {"app.url=https://test-url.pl"})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WeatherControllerIntegrationTest {

    private static final String LOCATION_COORDINATES_URL = "https://nominatim.openstreetmap.org/search?city=Wrocław&format=jsonv2&addressdetails=1&limit=1";
    private static final String RADAR_VIEWER_URL = "https://api.rainviewer.com/public/weather-maps.json";
    private static final String WEATHER_API_URL = "https://api.met.no/weatherapi/locationforecast/2.0/mini?lat=51.1089776&lon=17.0326689";

    @Autowired
    private RestClient.Builder restClientBuilder;

    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
    }

    @Test
    void shouldCallLocationApiAndReturnCoordinatesForGivenCity() throws Exception {
        //given
        Location location = new Location();
        location.setPlaceId(167984640);
        location.setLat(51.1089776);
        location.setLng(17.0326689);
        location.setName("Wrocław");
        location.setDisplayName("Wrocław, województwo dolnośląskie, Polska");
        Location[] mockResponse = {location};

        mockServer.expect(requestTo(LOCATION_COORDINATES_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/weather/location/coordinates")
                        .param("city", "Wrocław")
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        //when
        Coordinates actual = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Coordinates.class);

        //then
        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.getLat()).isEqualTo(location.getLat());
        Assertions.assertThat(actual.getLng()).isEqualTo(location.getLng());
    }

    @Test
    void shouldCallLocationApiAndReturnNoCoordinatesAndStatus400WhenNoCityIsGiven() throws Exception {
        //given
        mockServer.expect(requestTo("https://nominatim.openstreetmap.org/search?city=&format=jsonv2&addressdetails=1&limit=1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withBadRequest());

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/weather/location/coordinates")
                        .param("city", "")
                        .accept(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();

        //when
        int actual = mvcResult.getResponse().getStatus();

        //then
        Assertions.assertThat(actual).isEqualTo(400);
    }

    @Test
    void shouldCallWeatherCastApiAndReturnWeatherNowcastForGivenCoordinates() throws Exception {
        //given
        RainViewer mockResponse = new RainViewer();
        Radar radar = new Radar();
        Nowcast nowcast = new Nowcast();
        nowcast.setPath("/v2/radar/nowcast_15b401042a27");
        nowcast.setTime(1741804200);
        List<Nowcast> nowcastList = List.of(nowcast);
        radar.setNowcast(nowcastList);
        mockResponse.setRadar(radar);
        mockServer.expect(requestTo(RADAR_VIEWER_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/weather/nowcast")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .param("mapZoom", String.valueOf(1))
                        .content(objectMapper.writeValueAsString(new Coordinates(51.1089776, 17.0326689)))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        //when
        WeatherNowcast actual = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), WeatherNowcast.class);

        //then
        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.getNowcast().get(0).getPath()).contains("/v2/radar/nowcast_15b401042a27");
        Assertions.assertThat(actual.getNowcast().get(0).getTime()).isEqualTo(1741804200);

    }

    @Test
    void shouldCallWeatherCastApiAndReturn404StatusAndNoWeatherNowcastWhenNoCoordinatesAreGiven() throws Exception {
        //given
        mockServer.expect(requestTo(RADAR_VIEWER_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound());

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/weather/nowcast")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .param("mapZoom", String.valueOf(1))
                        .content(objectMapper.writeValueAsString(new Coordinates(null, null)))
                )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

        //when
        int status = mvcResult.getResponse().getStatus();

        //then
        Assertions.assertThat(status).isEqualTo(404);
    }

    @Test
    void shouldCallWeatherInfoApiAndReturnUpdatedWeatherInfoForGivenCoordinates() throws Exception {
        //given
        WeatherInfo weatherInfo = new WeatherInfo();
        Properties properties = new Properties();
        Timeseries timeseries = new Timeseries();
        Data data = new Data();
        Instant instant = new Instant();
        Details details = new Details();
        details.setAirTemperature(28.0);
        details.setWindSpeed(35.0);
        details.setAirPressureAtSeaLevel(0.1);
        instant.setDetails(details);
        data.setInstant(instant);
        timeseries.setData(data);
        timeseries.setTime(LocalDateTime.now());
        properties.setTimeseries(List.of(timeseries));
        weatherInfo.setProperties(properties);

        mockServer.expect(requestTo(WEATHER_API_URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(weatherInfo), MediaType.APPLICATION_JSON));
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/weather/info")
                        .param("lat", String.valueOf(51.1089776))
                        .param("lng", String.valueOf(17.0326689))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        //when
        Timeseries actual = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), Timeseries.class);

        //then
        Assertions.assertThat(actual).isNotNull();
        Assertions.assertThat(actual.getData().getInstant().getDetails().getAirTemperature()).isEqualTo(28.0);
        Assertions.assertThat(actual.getData().getInstant().getDetails().getWindSpeed()).isEqualTo(35.0);
        Assertions.assertThat(actual.getData().getInstant().getDetails().getAirPressureAtSeaLevel()).isEqualTo(0.1);
    }

    @Test
    void shouldCallWeatherInfoApiAndNReturn404StatusAndNoUpdatedWeatherInfoWhenCoordinatesAreWronglyGiven() throws Exception {
        //given
        mockServer.expect(requestTo("https://api.met.no/weatherapi/locationforecast/2.0/mini?lat=-3000.0&lon=-1800.0"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound());
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/weather/info")
                        .param("lat", String.valueOf(-3000))
                        .param("lng", String.valueOf(-1800))
                )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

        //when
        int status = mvcResult.getResponse().getStatus();

        //then
        Assertions.assertThat(status).isEqualTo(404);
    }
}