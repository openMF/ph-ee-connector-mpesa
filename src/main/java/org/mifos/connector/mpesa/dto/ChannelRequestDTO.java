package org.mifos.connector.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONObject;

import java.util.List;

@Getter
@Setter
public class ChannelRequestDTO {
    @JsonProperty("primaryIdentifier")
    private JSONObject primaryIdentifier;
    @JsonProperty("secondaryIdentifier")
    private JSONObject secondaryIdentifier;
    @JsonProperty("customData")
    private List<JSONObject> customData;

    @Override
    public String toString() {
        return "{" +
                "primaryIdentifier:" + primaryIdentifier +
                ", secondaryIdentifier:" + secondaryIdentifier +
                ", customData:" + customData +
                "}";
    }

    public ChannelRequestDTO() {
    }

    public ChannelRequestDTO(JSONObject primaryIdentifier, JSONObject secondaryIdentifier, List<JSONObject> customData) {
        this.primaryIdentifier = primaryIdentifier;
        this.secondaryIdentifier = secondaryIdentifier;
        this.customData = customData;
    }
}
