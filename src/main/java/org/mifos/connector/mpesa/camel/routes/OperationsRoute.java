package org.mifos.connector.mpesa.camel.routes;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.ListJacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.util.json.JsonArray;
import org.mifos.connector.mpesa.dto.ErrorCode;
import org.mifos.connector.mpesa.flowcomponents.transaction.ErrorProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;



import static org.mifos.connector.mpesa.camel.config.CamelProperties.*;
import static org.mifos.connector.mpesa.camel.config.OperationsProperties.FILTER_BY_ERROR_CODE;
import static org.mifos.connector.mpesa.camel.config.OperationsProperties.FILTER_BY_RECOVERABLE;

@Component
public class OperationsRoute extends RouteBuilder {

    @Value("${operations.host}")
    private String operationsHost;

    @Value("${operations.base-url}")
    private String operationsBaseUrl;

    @Value("${operations.filter-path}")
    private String operationsFilterPath;

    @Value("${tenant}")
    private String tenantId;

    @Autowired
    private ErrorProcessor errorProcessor;

    @Override
    public void configure() {

        from("rest:get:lest/filter")
                .id("filter-test")
                .process(exchange -> {
                    exchange.setProperty(ERROR_CODE, "1037");
                    exchange.setProperty("tenantId", "oaf");
                })
                .to("direct:filter-by-error-code")
                .process(exchange -> {
                    boolean isRe = exchange.getProperty(IS_ERROR_RECOVERABLE, Boolean.class);
                    exchange.getIn().setBody(""+isRe);
                });

        from("direct:filter-by-error-code")
                .id("filter-by-error-codes")
                .log(LoggingLevel.INFO, "### Starting FILTER-BY-ERROR-CODE route")
                .removeHeader("*")
                .removeHeader("Authorization")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader("Content-Type", constant("application/json"))
                .setHeader("Platform-TenantId", constant(tenantId))
                .setHeader(Exchange.HTTP_RAW_QUERY,
                        simple("by=" + FILTER_BY_ERROR_CODE + "&value=${exchangeProperty." + ERROR_CODE + "}"))
                .toD(getFilterUrl())
                .log(LoggingLevel.INFO, "Status: ${header.CamelHttpResponseCode}")
                .log(LoggingLevel.INFO, "Operations response: \n\n.. ${body}")
                .to("direct:filter-response-handler");

        from("direct:get-recoverable-error-codes")
                .id("get-recoverable-error-codes")
                .log(LoggingLevel.INFO, "### Starting GET-RECOVERABLE-CODES route")
                .removeHeader("*")
                .removeHeader("Authorization")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader("Content-Type", constant("application/json"))
                .setHeader("Platform-TenantId", constant(tenantId))
                .setHeader(Exchange.HTTP_RAW_QUERY,
                        simple("by=" + FILTER_BY_ERROR_CODE + "&value=${exchangeProperty." + ERROR_CODE + "}"))
                .toD(getFilterUrl())
                .log(LoggingLevel.INFO, "Operations response: \n\n.. ${body}")
                .to("direct:filter-response-handler");

        from("direct:get-non-recoverable-error-codes")
                .id("get-non-recoverable-error-codes")
                .log(LoggingLevel.INFO, "### Starting GET-NON-RECOVERABLE-CODES route")
                .removeHeader("*")
                .removeHeader("Authorization")
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .setHeader("Content-Type", constant("application/json"))
                .setHeader("Platform-TenantId", constant(tenantId))
                .setHeader(Exchange.HTTP_RAW_QUERY,
                        simple("by=" + FILTER_BY_ERROR_CODE + "&value=${exchangeProperty." + ERROR_CODE + "}"))
                .toD(getFilterUrl())
                .log(LoggingLevel.INFO, "Operations response: \n\n.. ${body}")
                .to("direct:filter-response-handler");

        from("direct:filter-response-handler")
                .id("filter-response-handler")
                .unmarshal(new ListJacksonDataFormat(ErrorCode.class))
                .log(LoggingLevel.INFO, "### Starting FILTER-RESPONSE-HANDLER route")
                .process(errorProcessor);

    }

    private String getFilterUrl() {
        String url = operationsHost + operationsBaseUrl + operationsFilterPath;
        String internalParams = "?bridgeEndpoint=true&throwExceptionOnFailure=false";
        return url + internalParams;
    }
}
