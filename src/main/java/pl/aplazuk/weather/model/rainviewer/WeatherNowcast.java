package pl.aplazuk.weather.model.rainviewer;

import java.util.List;

public class WeatherNowcast {
    private String host;
    private List<Nowcast> nowcast;

    public WeatherNowcast(String host, List<Nowcast> nowcast) {
        this.host = host;
        this.nowcast = nowcast;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public List<Nowcast> getNowcast() {
        return nowcast;
    }

    public void setNowcast(List<Nowcast> nowcast) {
        this.nowcast = nowcast;
    }
}
