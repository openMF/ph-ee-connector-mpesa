package org.mifos.connector.mpesa.camel.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mifos.connector.common.gsma.dto.RequestStateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ValidationRoutes extends RouteBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
                .unmarshal().json(JsonLibrary.Jackson, RequestStateDTO.class)
                .process(exchange -> {
                    String resp = exchange.getIn().getBody(String.class);
                    logger.info(resp);
                })
                .otherwise()
                .log(LoggingLevel.ERROR, "Transaction request unsuccessful")
                .process(exchange -> {
                    exchange.setProperty(TRANSACTION_ID, exchange.getProperty(CORRELATION_ID)); // TODO: Improve this
                })
                .setProperty(TRANSACTION_FAILED, constant(true))
                .process(transferResponseProcessor);

        from("rest:GET:/test/validation")
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
                .setBody(exchange -> "");

    }

}
