
package pl.aplazuk.weather.model.rainviewer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "past",
    "nowcast"
})
public class Radar {

    @JsonProperty("past")
    private List<Past> past;
    @JsonProperty("nowcast")
    private List<Nowcast> nowcast;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("past")
    public List<Past> getPast() {
        return past;
    }

    @JsonProperty("past")
    public void setPast(List<Past> past) {
        this.past = past;
    }

    @JsonProperty("nowcast")
    public List<Nowcast> getNowcast() {
        return nowcast;
    }

    @JsonProperty("nowcast")
    public void setNowcast(List<Nowcast> nowcast) {
        this.nowcast = nowcast;
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
