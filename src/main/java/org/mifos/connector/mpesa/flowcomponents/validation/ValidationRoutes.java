package org.mifos.connector.mpesa.flowcomponents.validation;

import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mifos.connector.common.gsma.dto.RequestStateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.mifos.connector.mpesa.camel.CamelProperties.ERROR_INFORMATION;
import static org.mifos.connector.mpesa.camel.CamelProperties.TRANSACTION_ID;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.IS_VALID_TRANSACTION;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.VALIDATION_RESPONSE;

@Component
public class ValidationRoutes extends RouteBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ValidationProcessor validationProcessor;

    @Autowired
    private ZeebeClient zeebeClient;

    @Value("${zeebe.client.ttl}")
    private int timeToLive;

    @Override
    public void configure() {

        from("direct:transaction-validation")
                .id("transaction-validation")
                .toD("https://localhost:5000/test/validation"+"?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .log(LoggingLevel.INFO, "Status: ${header.CamelHttpResponseCode}")
                .log(LoggingLevel.INFO, "Transaction API response: ${body}")
                .to("direct:validation-response-handler");

        from("direct:validation-response-handler")
                .id("transaction-response-handler")
                .choice()
                .when(header("CamelHttpResponseCode").isEqualTo("200"))
                .log(LoggingLevel.INFO, "Transaction request successful")
                //.unmarshal().json(JsonLibrary.Jackson, RequestStateDTO.class)
                .process(exchange -> {
                    String resp = exchange.getIn().getBody(String.class);
                    logger.info(resp);
                })
                .otherwise()
                .setProperty(IS_VALID_TRANSACTION, constant(false))
                .process(validationProcessor);

        from("rest:GET:/test/validation")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setBody(exchange -> "");

    }

}
