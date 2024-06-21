
package pl.aplazuk.weather.model.location;

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
    "administrative",
    "state",
    "ISO3166-2-lvl4",
    "country",
    "country_code"
})
public class Address {

    @JsonProperty("city")
    private String administrative;
    @JsonProperty("state")
    private String state;
    @JsonProperty("ISO3166-2-lvl4")
    private String iSO31662Lvl4;
    @JsonProperty("country")
    private String country;
    @JsonProperty("country_code")
    private String countryCode;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("administrative")
    public String getAdministrative() {
        return administrative;
    }

    @JsonProperty("administrative")
    public void setAdministrative(String administrative) {
        this.administrative = administrative;
    }

    @JsonProperty("state")
    public String getState() {
        return state;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("ISO3166-2-lvl4")
    public String getISO31662Lvl4() {
        return iSO31662Lvl4;
    }

    @JsonProperty("ISO3166-2-lvl4")
    public void setISO31662Lvl4(String iSO31662Lvl4) {
        this.iSO31662Lvl4 = iSO31662Lvl4;
    }

    @JsonProperty("country")
    public String getCountry() {
        return country;
    }

    @JsonProperty("country")
    public void setCountry(String country) {
        this.country = country;
    }

    @JsonProperty("country_code")
    public String getCountryCode() {
        return countryCode;
    }

    @JsonProperty("country_code")
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
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
