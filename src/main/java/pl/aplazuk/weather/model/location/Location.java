
package pl.aplazuk.weather.model.location;

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
    "place_id",
    "licence",
    "osm_type",
    "osm_id",
    "lat",
    "lon",
    "category",
    "type",
    "place_rank",
    "importance",
    "addresstype",
    "name",
    "display_name",
    "address",
    "boundingbox"
})
public class Location {

    @JsonProperty("place_id")
    private Integer placeId;
    @JsonProperty("licence")
    private String licence;
    @JsonProperty("osm_type")
    private String osmType;
    @JsonProperty("osm_id")
    private Integer osmId;
    @JsonProperty("lat")
    private Double lat;
    @JsonProperty("lon")
    private Double lng;
    @JsonProperty("category")
    private String category;
    @JsonProperty("type")
    private String type;
    @JsonProperty("place_rank")
    private Integer placeRank;
    @JsonProperty("importance")
    private Double importance;
    @JsonProperty("addresstype")
    private String addresstype;
    @JsonProperty("name")
    private String name;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("address")
    private Address address;
    @JsonProperty("boundingbox")
    private List<String> boundingbox;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    @JsonProperty("place_id")
    public Integer getPlaceId() {
        return placeId;
    }

    @JsonProperty("place_id")
    public void setPlaceId(Integer placeId) {
        this.placeId = placeId;
    }

    @JsonProperty("licence")
    public String getLicence() {
        return licence;
    }

    @JsonProperty("licence")
    public void setLicence(String licence) {
        this.licence = licence;
    }

    @JsonProperty("osm_type")
    public String getOsmType() {
        return osmType;
    }

    @JsonProperty("osm_type")
    public void setOsmType(String osmType) {
        this.osmType = osmType;
    }

    @JsonProperty("osm_id")
    public Integer getOsmId() {
        return osmId;
    }

    @JsonProperty("osm_id")
    public void setOsmId(Integer osmId) {
        this.osmId = osmId;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    @JsonProperty("category")
    public String getCategory() {
        return category;
    }

    @JsonProperty("category")
    public void setCategory(String category) {
        this.category = category;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("place_rank")
    public Integer getPlaceRank() {
        return placeRank;
    }

    @JsonProperty("place_rank")
    public void setPlaceRank(Integer placeRank) {
        this.placeRank = placeRank;
    }

    @JsonProperty("importance")
    public Double getImportance() {
        return importance;
    }

    @JsonProperty("importance")
    public void setImportance(Double importance) {
        this.importance = importance;
    }

    @JsonProperty("addresstype")
    public String getAddresstype() {
        return addresstype;
    }

    @JsonProperty("addresstype")
    public void setAddresstype(String addresstype) {
        this.addresstype = addresstype;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("display_name")
    public String getDisplayName() {
        return displayName;
    }

    @JsonProperty("display_name")
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @JsonProperty("address")
    public Address getAddress() {
        return address;
    }

    @JsonProperty("address")
    public void setAddress(Address address) {
        this.address = address;
    }

    @JsonProperty("boundingbox")
    public List<String> getBoundingbox() {
        return boundingbox;
    }

    @JsonProperty("boundingbox")
    public void setBoundingbox(List<String> boundingbox) {
        this.boundingbox = boundingbox;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return "Location{" +
                "placeId=" + placeId +
                ", licence='" + licence + '\'' +
                ", osmType='" + osmType + '\'' +
                ", osmId=" + osmId +
                ", lat=" + lat +
                ", lng=" + lng +
                ", category='" + category + '\'' +
                ", type='" + type + '\'' +
                ", placeRank=" + placeRank +
                ", importance=" + importance +
                ", addresstype='" + addresstype + '\'' +
                ", name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", address=" + address +
                ", boundingbox=" + boundingbox +
                ", additionalProperties=" + additionalProperties +
                '}';
    }
}
