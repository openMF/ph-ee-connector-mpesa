package org.mifos.connector.mpesa.flowcomponents.transaction;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.json.JsonArray;
import org.apache.camel.util.json.JsonObject;
import org.springframework.stereotype.Component;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.IS_ERROR_RECOVERABLE;
import static org.mifos.connector.mpesa.camel.config.OperationsProperties.FILTER_BY_RECOVERABLE;

@Component
public class ErrorProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        JsonArray response = exchange.getIn().getBody(JsonArray.class);

        if(response.size() == 0) {
            exchange.setProperty(IS_ERROR_RECOVERABLE, false);
            return;
        }

        JsonObject errorCodeObject = (JsonObject) response.get(0);
        Boolean recoverable = errorCodeObject.getBoolean(FILTER_BY_RECOVERABLE);
        exchange.setProperty(IS_ERROR_RECOVERABLE, recoverable);
    }
}
