package org.mifos.connector.mpesa.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Safaricom OAuth token response ({"access_token": "...", "expires_in": "3599"}).
 *
 * Replaces org.mifos.connector.common.gsma.dto.AccessTokenDTO as unmarshal target:
 * since connector-common 2.x that DTO has camelCase fields without Jackson
 * annotations, so the snake_case OAuth2 response no longer maps onto it.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessTokenResponseDTO {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private int expiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }
}
