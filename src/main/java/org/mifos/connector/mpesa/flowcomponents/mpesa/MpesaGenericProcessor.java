package org.mifos.connector.mpesa.flowcomponents.mpesa;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;
import org.apache.camel.Processor;

import static org.mifos.connector.mpesa.camel.config.CamelProperties.MPESA_API_RESPONSE;

@Component
public class MpesaGenericProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        exchange.setProperty(MPESA_API_RESPONSE, exchange.getIn().getBody(String.class));
    }

}
