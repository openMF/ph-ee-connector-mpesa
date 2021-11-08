package org.mifos.connector.mpesa.auth;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mifos.connector.common.gsma.dto.AccessTokenDTO;
import org.mifos.connector.common.gsma.dto.AuthErrorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

@Component
public class AuthRoutes extends RouteBuilder {

    @Value("${mpesa.auth.host}")
    private String authUrl;

    @Value("${mpesa.auth.client-key}")
    private String clientKey;

    @Value("${mpesa.auth.client-secret}")
    private String clientSecret;

    @Autowired
    private AccessTokenStore accessTokenStore;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void configure() {

        from("rest:GET:/auth")
                .id("authentication")
                .to("direct:get-access-token");

        /**
         * Error handling route
         */
        from("direct:access-token-error")
                .id("access-token-error")
                .unmarshal().json(JsonLibrary.Jackson, AuthErrorDTO.class)
                .process(exchange -> {
                    logger.error(exchange.getIn().getBody(AuthErrorDTO.class).getErrorMessage());
                    // TODO: Improve Error Handling
                });

        /**
         * Save Access Token to AccessTokenStore
         */
        from("direct:access-token-save")
                .id("access-token-save")
                .unmarshal().json(JsonLibrary.Jackson, AccessTokenDTO.class)
                .process(exchange -> {
                    accessTokenStore.setAccessToken(exchange.getIn().getBody(AccessTokenDTO.class).getAccess_token());
                    accessTokenStore.setExpiresOn(exchange.getIn().getBody(AccessTokenDTO.class).getExpires_in());
                    logger.info("Saved Access Token: " + accessTokenStore.getAccessToken());
                });

        /**
         * Fetch Access Token from mpesa API
         */
        from("direct:access-token-fetch")
                .id("access-token-fetch")
                .removeHeader("*")
                .log(LoggingLevel.INFO, "Fetching access token")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader("Authorization", simple("Basic " + createAuthHeader(clientKey, clientSecret)))
                .removeHeader(Exchange.HTTP_PATH)
                .process(exchange -> {
                    String val = (String) exchange.getIn().getHeader("Authorization");
                    logger.info(val);
                })
                .toD(authUrl + "&bridgeEndpoint=true");

        /**
         * Access Token check validity and return value
         */
        from("direct:get-access-token")
                .id("get-access-token")
                .choice()
                .when(exchange -> accessTokenStore.isValid(LocalDateTime.now()))
                .log("Access token valid. Continuing.")
                .otherwise()
                .log("Access token expired or not present")
                .to("direct:access-token-fetch")
                .choice()
                .when(header("CamelHttpResponseCode").isEqualTo("200"))
                .log("Access Token Fetch Successful")
                .to("direct:access-token-save")
                .otherwise()
                .log("Access Token Fetch Unsuccessful")
                .to("direct:access-token-error");

    }

    private String createAuthHeader(String key, String secret) {
        byte[] credential = (key+":"+secret).getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(credential);
    }


}
