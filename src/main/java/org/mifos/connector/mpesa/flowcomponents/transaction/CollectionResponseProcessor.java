package org.mifos.connector.mpesa.flowcomponents.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.Exchange;
import org.apache.camel.util.json.JsonObject;
import org.mifos.connector.mpesa.utility.ZeebeUtils;
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

    private final ZeebeClient zeebeClient;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${zeebe.client.ttl}")
    private int timeToLive;

    public CollectionResponseProcessor(ZeebeClient zeebeClient) {
        this.zeebeClient = zeebeClient;
    }

    @Override
    public void process(Exchange exchange) throws JsonProcessingException {
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

        String clientCorrelationId = exchange.getProperty(TRANSACTION_ID, String.class);

        if(clientCorrelationId == null) {
            JsonObject response = new JsonObject();
            response.put("developerMessage", "Can't find the correlation ID for the provided callback, with server id " +
                    exchange.getProperty(SERVER_TRANSACTION_ID) + "It might be possible that either transaction doesn't " +
                    "exist or this is test hit");
            response.put("zeebeVariables", objectMapper.writeValueAsString(variables));
            exchange.getIn().setBody(response.toJson());
            return;
        }
        logger.info("Publishing transaction message variables: " + variables);
        zeebeClient.newPublishMessageCommand()
                .messageName(TRANSFER_MESSAGE)
                .correlationKey(clientCorrelationId)
                .timeToLive(Duration.ofMillis(timeToLive))
                .variables(variables)
                .send()
                .join();
    }
}
