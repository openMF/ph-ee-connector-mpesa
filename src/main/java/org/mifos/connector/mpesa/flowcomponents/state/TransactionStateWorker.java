package org.mifos.connector.mpesa.flowcomponents.state;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.mifos.connector.common.channel.dto.TransactionChannelCollectionRequestDTO;
import org.mifos.connector.mpesa.dto.BuyGoodsPaymentRequestDTO;
import org.mifos.connector.mpesa.utility.SafaricomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.Map;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.*;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.*;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.TRANSACTION_ID;

@Component
public class TransactionStateWorker {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private SafaricomUtils safaricomUtils;

    @Value("${zeebe.client.evenly-allocated-max-jobs}")
    private int workerMaxJobs;

    @PostConstruct
    public void setupWorkers() {

        zeebeClient.newWorker()
                .jobType("get-transaction-status")
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                    Map<String, Object> variables = job.getVariablesAsMap();
                    Integer retryCount = 1 + (Integer) variables.getOrDefault(TRANSFER_RETRY_COUNT, 0);
                    variables.put(TRANSFER_RETRY_COUNT, retryCount);
                    logger.info("Trying count: " + retryCount);
                    TransactionChannelCollectionRequestDTO channelRequest = objectMapper.readValue(
                            (String) variables.get("mpesaChannelRequest"), TransactionChannelCollectionRequestDTO .class);
                    BuyGoodsPaymentRequestDTO buyGoodsPaymentRequestDTO = safaricomUtils.channelRequestConvertor(
                            channelRequest);
                    Exchange exchange = new DefaultExchange(camelContext);
                    exchange.setProperty(CORRELATION_ID, variables.get("transactionId"));
                    exchange.setProperty(TRANSACTION_ID, variables.get("transactionId"));
                    exchange.setProperty(SERVER_TRANSACTION_ID, variables.get(SERVER_TRANSACTION_ID));
                    exchange.setProperty(BUY_GOODS_REQUEST_BODY, buyGoodsPaymentRequestDTO);
                    exchange.setProperty(SERVER_TRANSACTION_STATUS_RETRY_COUNT, retryCount);
                    exchange.setProperty(ZEEBE_ELEMENT_INSTANCE_KEY, job.getElementInstanceKey());

                    producerTemplate.send("direct:get-transaction-status-base", exchange);

                    /*variables.put(STATUS_AVAILABLE, exchange.getProperty(STATUS_AVAILABLE, Boolean.class));
                    if (exchange.getProperty(STATUS_AVAILABLE, Boolean.class)) {
                        variables.put(TRANSACTION_STATUS, exchange.getProperty(TRANSACTION_STATUS, String.class));
                    }*/

                    client.newCompleteCommand(job.getKey())
                            .send()
                            .join();
                })
                .name("get-transaction-status")
                .maxJobsActive(workerMaxJobs)
                .open();

    }


}
