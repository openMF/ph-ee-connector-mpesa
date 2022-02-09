package org.mifos.connector.mpesa.camel.routes;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.util.json.JsonArray;
import org.mifos.connector.mpesa.flowcomponents.transaction.ErrorProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.OPERATIONS_FILTER_VALUE;
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

    @Autowired
    private ErrorProcessor errorProcessor;

    @Override
    public void configure() throws Exception {

        from("direct:filter-by-error-code")
                .id("filter-by-error-codes")
                .log(LoggingLevel.INFO, "### Starting FILTER-BY-ERROR-CODE route")
                .toD(getFilterUrl(FILTER_BY_ERROR_CODE, exchangeProperty(OPERATIONS_FILTER_VALUE)))
                .log(LoggingLevel.INFO, "Operations response: \n\n.. ${body}")
                .to("direct:filter-response-handler");

        from("direct:get-recoverable-error-codes")
                .id("get-recoverable-error-codes")
                .log(LoggingLevel.INFO, "### Starting GET-RECOVERABLE-CODES route")
                .toD(getFilterUrl(FILTER_BY_RECOVERABLE, true))
                .log(LoggingLevel.INFO, "Operations response: \n\n.. ${body}")
                .to("direct:filter-response-handler");

        from("direct:get-non-recoverable-error-codes")
                .id("get-non-recoverable-error-codes")
                .log(LoggingLevel.INFO, "### Starting GET-NON-RECOVERABLE-CODES route")
                .toD(getFilterUrl(FILTER_BY_RECOVERABLE, false))
                .log(LoggingLevel.INFO, "Operations response: \n\n.. ${body}")
                .to("direct:filter-response-handler");

        from("direct:filter-response-handler")
                .id("filter-response-handler")
                .unmarshal().json(JsonLibrary.Jackson, JsonArray.class)
                .log(LoggingLevel.INFO, "### Starting FILTER-RESPONSE-HANDLER route")
                .process(errorProcessor);

    }

    private String getFilterUrl(String filterBy, Object filterValue) {
        String url = operationsHost + operationsBaseUrl + operationsFilterPath;
        String params = String.format("?by=%s&value=%s&", filterBy, filterValue);
        String internalParams = "bridgeEndpoint=true&throwExceptionOnFailure=false";
        return url + params + internalParams;
    }
}
