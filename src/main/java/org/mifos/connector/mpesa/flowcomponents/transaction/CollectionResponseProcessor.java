package org.mifos.connector.mpesa.flowcomponents.transaction;

import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.Exchange;
import org.mifos.connector.mpesa.utility.ZeebeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final ZeebeClient zeebeClient;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${zeebe.client.ttl}")
    private int timeToLive;

    public CollectionResponseProcessor(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }

    @Override
    public void process(Exchange exchange) {
        Map<String, Object> variables = new HashMap<>();

        Object hasTransferFailed = exchange.getProperty(TRANSACTION_FAILED);

        if (hasTransferFailed != null && (boolean)hasTransferFailed) {
            String body = exchange.getIn().getBody(String.class);
            variables.put(TRANSACTION_FAILED, true);
            variables.put(TRANSFER_CREATE_FAILED, true);
            variables.put(ERROR_INFORMATION, body);
        } else {
            variables.put(TRANSACTION_FAILED, false);
            variables.put(TRANSFER_CREATE_FAILED, false);
            variables.put(SERVER_TRANSACTION_ID, exchange.getProperty(SERVER_TRANSACTION_ID));
        }

        variables.put(TRANSFER_RESPONSE_CREATE, ZeebeUtils.getTransferResponseCreateJson());

        logger.info("Publishing transaction message variables: " + variables);

        String id = exchange.getProperty(TRANSACTION_ID, String.class);

        zeebeClient.newPublishMessageCommand()
                .messageName(TRANSFER_MESSAGE)
                .correlationKey(id)
                .timeToLive(Duration.ofMillis(timeToLive))
                .variables(variables)
                .send()
                .join();
    }
}
