package org.mifos.connector.mpesa.flowcomponents.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.mifos.connector.common.channel.dto.TransactionChannelRequestDTO;
import org.mifos.connector.mpesa.dto.BuyGoodsPaymentRequestDTO;
import org.mifos.connector.mpesa.utility.EntityMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

import static org.mifos.connector.mpesa.camel.CamelProperties.*;
import static org.mifos.connector.mpesa.camel.CamelProperties.TRANSACTION_TYPE;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.BUY_GOODS_REQUEST_BODY;

@Component
public class MpesaWorker {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    EntityMapper<TransactionChannelRequestDTO, BuyGoodsPaymentRequestDTO>
            channelRequestDTOToBuyGoodsPaymentRequestDTOEntityMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext;

    @Value("${zeebe.client.evenly-allocated-max-jobs}")
    private int workerMaxJobs;

    @PostConstruct
    public void setupWorkers() {

        zeebeClient.newWorker()
                .jobType("init-transfer")
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());

                    Map<String, Object> variables = job.getVariablesAsMap();
                    TransactionChannelRequestDTO channelRequest = objectMapper.readValue(
                            (String) variables.get("channelRequest"), TransactionChannelRequestDTO.class);

                    BuyGoodsPaymentRequestDTO buyGoodsPaymentRequestDTO =
                            channelRequestDTOToBuyGoodsPaymentRequestDTOEntityMapper.fromEntityToDomain(channelRequest);


                    Exchange exchange = new DefaultExchange(camelContext);
                    exchange.setProperty(BUY_GOODS_REQUEST_BODY, buyGoodsPaymentRequestDTO);

                    producerTemplate.send("direct:buy-goods-base", exchange);

                    client.newCompleteCommand(job.getKey())
                            .send()
                            .join();
                })
                .name("init-transfer")
                .maxJobsActive(workerMaxJobs)
                .open();

        zeebeClient.newWorker()
                .jobType("get-transaction-status")
                .handler((client, job) -> {
                    // TODO IMPLEMENTATION
                })
                .name("get-transaction-status")
                .maxJobsActive(workerMaxJobs)
                .open();

    }

}
