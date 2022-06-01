package org.mifos.connector.mpesa.auth;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mifos.connector.common.gsma.dto.AccessTokenDTO;
import org.mifos.connector.mpesa.utility.ConnectionUtils;
import org.mifos.connector.mpesa.utility.MpesaProps;
import org.mifos.connector.mpesa.utility.MpesaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

import static org.mifos.connector.mpesa.camel.config.CamelProperties.ERROR_INFORMATION;

@Component
public class AuthRoutes extends RouteBuilder {

    @Autowired
    private AccessTokenStore accessTokenStore;

    @Autowired
    private MpesaUtils mpesautils;

    @Value("${mpesa.api.timeout}")
    private Integer mpesaTimeout;

    private MpesaProps.MPESA mpesaProps;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void configure() {
        mpesaProps = mpesautils.setMpesaProperties();
        logger.info("AMS Name set by configure" + mpesaProps.getName());
        /*
          Error handling route
         */
        from("direct:access-token-error")
                .id("access-token-error")
                //.unmarshal().json(JsonLibrary.Jackson, AuthErrorDTO.class)
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    logger.error(body);
                    // TODO: Improve Error Handling
                    exchange.setProperty(ERROR_INFORMATION, body);
                });

        /*
          Save Access Token to AccessTokenStore
         */
        from("direct:access-token-save")
                .id("access-token-save")
                .unmarshal().json(JsonLibrary.Jackson, AccessTokenDTO.class)
                .process(exchange -> {
                    accessTokenStore.setAccessToken(exchange.getIn().getBody(AccessTokenDTO.class).getAccess_token());
                    accessTokenStore.setExpiresOn(exchange.getIn().getBody(AccessTokenDTO.class).getExpires_in());
                    logger.info("Saved Access Token: " + accessTokenStore.getAccessToken());
                });

        /*
          Fetch Access Token from mpesa API
         */
        from("direct:access-token-fetch")
                .id("access-token-fetch")
                .log(LoggingLevel.INFO, "Fetching access token")
                .removeHeader(Exchange.HTTP_PATH)
                .process(exchange -> {
                    mpesaProps = mpesautils.setMpesaProperties();
                    logger.info("AMS Name in Route " + mpesaProps.getName());
                })
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader("Authorization", simple("Basic " + createAuthHeader(mpesaProps.getClientKey(),mpesaProps.getClientSecret())))
                .setHeader(Exchange.HTTP_RAW_QUERY, constant("grant_type=client_credentials"))
                .toD(mpesaProps.getAuthHost() + "?bridgeEndpoint=true" + "&" +
                        "throwExceptionOnFailure=false&" + ConnectionUtils.getConnectionTimeoutDsl(mpesaTimeout));

        /*
          Access Token check validity and return value
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
        key = key.replace("\n", "");
        secret = secret.replace("\n", "");
        byte[] credential = (key+":"+secret).getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(credential);
    }


}
