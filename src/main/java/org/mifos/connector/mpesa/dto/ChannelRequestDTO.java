package org.mifos.connector.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.json.JSONObject;

import java.util.List;

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

    public JSONObject getPrimaryIdentifier() {
        return primaryIdentifier;
    }

    public void setPrimaryIdentifier(JSONObject primaryIdentifier) {
        this.primaryIdentifier = primaryIdentifier;
    }

    public JSONObject getSecondaryIdentifier() {
        return secondaryIdentifier;
    }

    public void setSecondaryIdentifier(JSONObject secondaryIdentifier) {
        this.secondaryIdentifier = secondaryIdentifier;
    }

    public List<JSONObject> getCustomData() {
        return customData;
    }

    public void setCustomData(List<JSONObject> customData) {
        this.customData = customData;
    }

    public ChannelRequestDTO() {
    }

    public ChannelRequestDTO(JSONObject primaryIdentifier, JSONObject secondaryIdentifier, List<JSONObject> customData) {
        this.primaryIdentifier = primaryIdentifier;
        this.secondaryIdentifier = secondaryIdentifier;
        this.customData = customData;
    }
}
