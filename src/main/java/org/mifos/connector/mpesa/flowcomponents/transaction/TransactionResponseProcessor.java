package org.mifos.connector.mpesa.flowcomponents.transaction;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;
import org.apache.camel.Processor;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.ERROR_INFORMATION;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.*;

@Component
public class TransactionResponseProcessor implements Processor{

    @Override
    public void process(Exchange exchange) {

        Object hasTransferFailed = exchange.getProperty(TRANSACTION_FAILED);

        if (hasTransferFailed != null && (boolean)hasTransferFailed) {
            String body = exchange.getIn().getBody(String.class);
            exchange.setProperty(TRANSACTION_FAILED, true);
            exchange.setProperty(ERROR_INFORMATION, body);
        } else {
            exchange.setProperty(TRANSACTION_FAILED, false);
            exchange.setProperty(SERVER_TRANSACTION_ID, exchange.getProperty(SERVER_TRANSACTION_ID));
        }

    }


}
