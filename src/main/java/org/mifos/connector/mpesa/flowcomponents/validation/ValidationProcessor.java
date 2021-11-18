package org.mifos.connector.mpesa.flowcomponents.validation;

import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.apache.camel.Processor;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.mifos.connector.mpesa.camel.CamelProperties.ERROR_INFORMATION;
import static org.mifos.connector.mpesa.camel.CamelProperties.TRANSACTION_ID;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.IS_VALID_TRANSACTION;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.VALIDATION_RESPONSE;

@Component
public class ValidationProcessor implements Processor {

    @Autowired
    private ZeebeClient zeebeClient;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${zeebe.client.ttl}")
    private int timeToLive;


    @Override
    public void process(Exchange exchange) throws Exception {

        Map<String, Object> variables = new HashMap<>();
        Object hasTransferFailed = exchange.getProperty(IS_VALID_TRANSACTION);

        if (hasTransferFailed != null && (boolean)hasTransferFailed) {
            variables.put(IS_VALID_TRANSACTION, false);
            variables.put(ERROR_INFORMATION, exchange.getIn().getBody(String.class));
        } else {
            variables.put(IS_VALID_TRANSACTION, true);
        }

        logger.info("Publishing validation variables: " + variables);

        zeebeClient.newPublishMessageCommand()
                .messageName(VALIDATION_RESPONSE)
                .correlationKey(exchange.getProperty(TRANSACTION_ID, String.class))
                .timeToLive(Duration.ofMillis(timeToLive))
                .variables(variables)
                .send()
                .join();

    }
}
