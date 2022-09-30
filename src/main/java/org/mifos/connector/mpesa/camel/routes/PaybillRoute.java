package org.mifos.connector.mpesa.camel.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.LoggingLevel;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mifos.connector.common.camel.ErrorHandlerRouteBuilder;
import org.mifos.connector.mpesa.dto.ChannelRequestDTO;
import org.mifos.connector.mpesa.dto.PaybillRequestDTO;
import org.mifos.connector.mpesa.utility.MpesaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class PaybillRoute extends ErrorHandlerRouteBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper objectMapper;
    @Autowired
    private Environment env;

    public PaybillRoute(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure() {
        from("rest:POST:/validation")
                .id("mpesa-validation")
                .unmarshal().json(JsonLibrary.Jackson, PaybillRequestDTO.class)
                .log(LoggingLevel.INFO, "## Paybill request payload")
                .setBody(e -> {
                    PaybillRequestDTO paybillRequestDTO = e.getIn().getBody(PaybillRequestDTO.class);
                    //Getting the ams name
                    String amsName = e.getIn().getHeader("amsName", String.class);
                    logger.info("AMS Name : {}", amsName);
                    //Channel URL
                    String channelUrl = env.getProperty("channel.host");
                    logger.info("Channel URL : {}", channelUrl);
                    e.setProperty("channelUrl", channelUrl);
                    e.setProperty("secondaryIdentifier", "MSISDN");
                    e.setProperty("secondaryIdentifierValue", paybillRequestDTO.getMsisdn());
                    ChannelRequestDTO obj = MpesaUtils.convertPaybillPayloadToChannelPayload(paybillRequestDTO, amsName);
                    logger.info("Paybill Object:{}", obj);
                    return obj.toString();
                })
                .log("MPESA Request Body : ${body}")
                .toD("${header.channelUrl}" + "/accounts/${header.secondaryIdentifier}/${header.secondaryIdentifierValue}" + "?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .choice()
                .when(header("CamelHttpResponseCode").isEqualTo("200"))
                .log(LoggingLevel.INFO, "Request sent to channel")
                .process(e -> {
                    e.setProperty("MPESA_WEBHOOK_SUCCESS", true);
                })
                .otherwise()
                .log(LoggingLevel.INFO, "Request failed to sent to channel")
                .process(e -> {
                    logger.info(e.getIn().getBody(String.class));
                    e.setProperty("MPESA_WEBHOOK_SUCCESS", false);
                    e.setProperty("ERROR_DESCRIPTION", e.getIn().getBody(String.class));
                    e.setProperty("ERROR_CODE", e.getIn().getHeader(e.HTTP_RESPONSE_CODE));
                });

        
    }
}
