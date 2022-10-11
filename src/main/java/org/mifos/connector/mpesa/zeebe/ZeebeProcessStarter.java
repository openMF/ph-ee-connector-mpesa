package org.mifos.connector.mpesa.zeebe;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Component
public class ZeebeProcessStarter {

    private static final Logger logger = LoggerFactory.getLogger(ZeebeProcessStarter.class);

    @Autowired
    private ZeebeClient zeebeClient;

    public static void zeebeVariablesToCamelHeaders(Map<String, Object> variables, Exchange exchange, String... names) {
        for (String name : names) {
            Object value = variables.get(name);
            if (value == null) {
                logger.error("failed to find Zeebe variable name {}", name);
            }
            exchange.getIn().setHeader(name, value);
        }
    }

    public static void camelHeadersToZeebeVariables(Exchange exchange, Map<String, Object> variables, String... names) {
        for (String name : names) {
            String header = exchange.getIn().getHeader(name, String.class);
            if (header == null) {
                logger.error("failed to find Camel Exchange header {}", name);
            }
            variables.put(name, header);
        }
    }

    public void startZeebeWorkflow(String workflowId, Map<String, Object> extraVariables) {
        Map<String, Object> variables = new HashMap<>();
        variables.putAll(extraVariables);
        // TODO: Add extra variables if required. Such as origin date.

        zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(workflowId)
                .latestVersion() // .version(1)
                .variables(variables)
                .send()
                .join();

        logger.info("zeebee workflow instance from process {} started", workflowId);
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString();
    }

    public String startZeebeWorkflowPaybill(String workflowId, Map<String, Object> variables) {

        String transactionId = generateTransactionId();
        variables.put(ZeebeVariables.TRANSACTION_ID, transactionId);

        logger.info("starting workflow HERE:");
        ProcessInstanceEvent instance = zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(workflowId)
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        logger.info("zeebee workflow instance from process {} started with transactionId {}, instance key: {}", workflowId, transactionId, instance.getProcessInstanceKey());
        return transactionId;
    }
}
