package org.mifos.connector.mpesa.flowcomponents.transaction;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.mifos.connector.mpesa.dto.ErrorCode;
import org.springframework.stereotype.Component;
import java.util.List;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.IS_ERROR_RECOVERABLE;

@Component
public class ErrorProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        List<ErrorCode> codes = exchange.getIn().getBody(List.class);

        if(codes.size() == 0) {
            exchange.setProperty(IS_ERROR_RECOVERABLE, true);
            return;
        }

        ErrorCode errorCode = codes.get(0);
        Boolean recoverable = errorCode.isRecoverable();
        exchange.setProperty(IS_ERROR_RECOVERABLE, recoverable);
        exchange.getIn().setBody("");
    }
}
