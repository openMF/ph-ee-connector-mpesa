package org.mifos.connector.mpesa.flowcomponents.transaction;

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

import static org.mifos.connector.mpesa.camel.config.CamelProperties.ERROR_INFORMATION;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.*;

@Component
public class CollectionResponseProcessor implements Processor {

    @Autowired
    private ZeebeClient zeebeClient;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${zeebe.client.ttl}")
    private int timeToLive;

    @Override
    public void process(Exchange exchange) {
        Map<String, Object> variables = new HashMap<>();

        Object hasTransferFailed = exchange.getProperty(TRANSACTION_FAILED);

        if (hasTransferFailed != null && (boolean)hasTransferFailed) {
            String body = exchange.getIn().getBody(String.class);
            variables.put(TRANSACTION_FAILED, true);
            variables.put(ERROR_INFORMATION, body);
            exchange.setProperty(TRANSACTION_FAILED, true);
            exchange.setProperty(ERROR_INFORMATION, body);
        } else {
            variables.put(TRANSACTION_FAILED, false);

            zeebeClient.newPublishMessageCommand()
                    .messageName(TRANSFER_MESSAGE)
                    .correlationKey(exchange.getProperty(TRANSACTION_ID, String.class))
                    .variables(variables)
                    .send()
                    .join();
        }

        logger.info("Publishing transaction message variables: " + variables);

        zeebeClient.newPublishMessageCommand()
                .messageName(TRANSFER_RESPONSE)
                .correlationKey(exchange.getProperty(TRANSACTION_ID, String.class))
                .timeToLive(Duration.ofMillis(timeToLive))
                .variables(variables)
                .send()
                .join();
    }
}
