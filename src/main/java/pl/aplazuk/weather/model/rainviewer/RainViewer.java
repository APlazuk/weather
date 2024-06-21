
package pl.aplazuk.weather.model.rainviewer;

import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "version",
    "generated",
    "host",
    "radar",
    "satellite"
})
public class RainViewer {

    @JsonProperty("version")
    private String version;
    @JsonProperty("generated")
    private Integer generated;
    @JsonProperty("host")
    private String host;
    @JsonProperty("radar")
    private Radar radar;
    @JsonProperty("satellite")
    private Satellite satellite;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("generated")
    public Integer getGenerated() {
        return generated;
    }

    @JsonProperty("generated")
    public void setGenerated(Integer generated) {
        this.generated = generated;
    }

    @JsonProperty("host")
    public String getHost() {
        return host;
    }

    @JsonProperty("host")
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty("radar")
    public Radar getRadar() {
        return radar;
    }

    @JsonProperty("radar")
    public void setRadar(Radar radar) {
        this.radar = radar;
    }

    @JsonProperty("satellite")
    public Satellite getSatellite() {
        return satellite;
    }

    @JsonProperty("satellite")
    public void setSatellite(Satellite satellite) {
        this.satellite = satellite;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
