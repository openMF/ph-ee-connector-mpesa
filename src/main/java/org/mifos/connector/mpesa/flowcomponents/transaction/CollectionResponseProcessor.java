package org.mifos.connector.mpesa.flowcomponents.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.Exchange;
import org.apache.camel.http.base.HttpOperationFailedException;
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
import static org.mifos.connector.mpesa.camel.config.CamelProperties.*;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.TRANSACTION_ID;
import static org.mifos.connector.mpesa.utility.ZeebeUtils.getNextTimer;
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
        Object updatedRetryCount = exchange.getProperty(SERVER_TRANSACTION_STATUS_RETRY_COUNT);
        if(updatedRetryCount != null) {
            variables.put(SERVER_TRANSACTION_STATUS_RETRY_COUNT, updatedRetryCount);
            String body = exchange.getProperty(LAST_RESPONSE_BODY, String.class);
            Object statusCode = exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE);
            if(body == null) {
                body = exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_TEXT, String.class);
            }
            if(statusCode == null) {
                Exception e = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
                if(null!=e && e instanceof HttpOperationFailedException)
                {
                    HttpOperationFailedException httpOperationFailedException = (HttpOperationFailedException)e;
                    statusCode=httpOperationFailedException.getStatusCode();
                }
            }
            variables.put(GET_TRANSACTION_STATUS_RESPONSE, body);
            variables.put(GET_TRANSACTION_STATUS_RESPONSE_CODE, exchange.getIn().getHeader(Exchange.HTTP_RESPONSE_CODE));
        }

        Boolean isRetryExceeded = (Boolean) exchange.getProperty(IS_RETRY_EXCEEDED);

        Object isTransactionPending = exchange.getProperty(IS_TRANSACTION_PENDING);

        if(isTransactionPending != null && (boolean) isTransactionPending &&
                (isRetryExceeded == null || !isRetryExceeded)) {
            String newTimer = getNextTimer(exchange.getProperty(TIMER, String.class));
            logger.info("Updating retry count to " + updatedRetryCount);
            logger.info("Updating timer value to " + newTimer);
            variables.put(TIMER, newTimer);
            Long elementInstanceKey = (Long) exchange.getProperty(ZEEBE_ELEMENT_INSTANCE_KEY);
            zeebeClient.newSetVariablesCommand(elementInstanceKey)
                    .variables(variables)
                    .send()
                    .join();
            return;
        }

        Object hasTransferFailed = exchange.getProperty(TRANSACTION_FAILED);

        if (hasTransferFailed != null && (boolean)hasTransferFailed) {
            String body = exchange.getProperty(ERROR_INFORMATION, String.class);
            variables.put(TRANSACTION_FAILED, true);
            variables.put(TRANSFER_CREATE_FAILED, true);
            if(isRetryExceeded == null || !isRetryExceeded) {
                variables.put(ERROR_INFORMATION, body);
                variables.put(ERROR_CODE, exchange.getProperty(ERROR_CODE, String.class));
                variables.put(ERROR_DESCRIPTION, exchange.getProperty(ERROR_DESCRIPTION, String.class));
            }
        } else {
            variables.put(TRANSACTION_FAILED, false);
            variables.put(TRANSFER_CREATE_FAILED, false);
            variables.put(SERVER_TRANSACTION_ID, exchange.getProperty(SERVER_TRANSACTION_ID));
            Object receiptNumber = exchange.getProperty(SERVER_TRANSACTION_RECEIPT_NUMBER);
            if(receiptNumber != null) {
                variables.put(SERVER_TRANSACTION_RECEIPT_NUMBER, receiptNumber);
            }
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
