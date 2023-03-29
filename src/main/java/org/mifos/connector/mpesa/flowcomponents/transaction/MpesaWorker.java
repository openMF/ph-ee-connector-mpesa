package org.mifos.connector.mpesa.flowcomponents.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.support.DefaultExchange;
import org.mifos.connector.common.channel.dto.TransactionChannelC2BRequestDTO;
import org.mifos.connector.mpesa.dto.BuyGoodsPaymentRequestDTO;
import org.mifos.connector.mpesa.utility.MpesaUtils;
import org.mifos.connector.mpesa.utility.SafaricomUtils;
import org.mifos.connector.mpesa.utility.ZeebeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

import static org.mifos.connector.mpesa.camel.config.CamelProperties.BUY_GOODS_REQUEST_BODY;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.CORRELATION_ID;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.DEPLOYED_PROCESS;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.ERROR_CODE;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.ERROR_DESCRIPTION;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.ERROR_INFORMATION;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.MPESA_API_RESPONSE;
import static org.mifos.connector.mpesa.camel.routes.PaybillRoute.workflowInstanceStore;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.SERVER_TRANSACTION_ID;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.TRANSACTION_FAILED;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.TRANSACTION_ID;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.TRANSFER_CREATE_FAILED;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.TRANSFER_RESPONSE_CREATE;

@Component
public class MpesaWorker {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ZeebeClient zeebeClient;

    @Autowired
    private ProducerTemplate producerTemplate;

    @Autowired
    private CamelContext camelContext;

    @Autowired
    private SafaricomUtils safaricomUtils;

    @Autowired
    private MpesaUtils mpesaUtils;

    @Value("${zeebe.client.evenly-allocated-max-jobs}")
    private int workerMaxJobs;

    @Value("${zeebe.init-transfer.wait-timer}")
    private int initTransferWaitTimer;

    @Value("${skip.enabled}")
    private Boolean skipMpesa;

    @PostConstruct
    public void setupWorkers() {

        zeebeClient.newWorker()
                .jobType("init-transfer")
                .handler((client, job) -> {
                    logger.info("Job '{}' started from process '{}' with key {}", job.getType(), job.getBpmnProcessId(), job.getKey());
                    long t1 = System.currentTimeMillis();
                    logger.info("Going to sleep at " + t1 + " for " + initTransferWaitTimer + " seconds");
                    ZeebeUtils.sleep(initTransferWaitTimer);
                    long t2 = System.currentTimeMillis();
                    logger.info("I am awake at " + t2);
                    logger.info("Time diff " + (t2 - t1));

                    Map<String, Object> variables = job.getVariablesAsMap();
                    mpesaUtils.setProcess(job.getBpmnProcessId());
                    if (skipMpesa) {
                        logger.info("Skipping MPESA");
                        Exchange exchange = new DefaultExchange(camelContext);
                        String serverTransactionId = exchange.getProperty(SERVER_TRANSACTION_ID, String.class);
                        variables.put(TRANSACTION_FAILED, false);
                        variables.put(TRANSFER_CREATE_FAILED, false);
                        variables.put(SERVER_TRANSACTION_ID, serverTransactionId);
                    } else {
                        TransactionChannelC2BRequestDTO channelRequest = objectMapper.readValue(
                                (String) variables.get("mpesaChannelRequest"), TransactionChannelC2BRequestDTO.class);
                        String transactionId = (String) variables.get(TRANSACTION_ID);

                        BuyGoodsPaymentRequestDTO buyGoodsPaymentRequestDTO = safaricomUtils.channelRequestConvertor(
                                channelRequest);
                        logger.info(buyGoodsPaymentRequestDTO.toString());
                        Exchange exchange = new DefaultExchange(camelContext);
                        exchange.setProperty(BUY_GOODS_REQUEST_BODY, buyGoodsPaymentRequestDTO);
                        exchange.setProperty(CORRELATION_ID, transactionId);
                        exchange.setProperty(DEPLOYED_PROCESS, job.getBpmnProcessId());

                        variables.put(BUY_GOODS_REQUEST_BODY, buyGoodsPaymentRequestDTO.toString());

                        producerTemplate.send("direct:buy-goods-base", exchange);
                        variables.put(MPESA_API_RESPONSE, exchange.getProperty(MPESA_API_RESPONSE));

                        boolean isTransactionFailed = exchange.getProperty(TRANSACTION_FAILED, boolean.class);
                        if (isTransactionFailed) {
                            variables.put(TRANSACTION_FAILED, true);
                            variables.put(TRANSFER_CREATE_FAILED, true);
                            variables.put(TRANSFER_RESPONSE_CREATE, ZeebeUtils.getTransferResponseCreateJson());
                            String errorBody = exchange.getProperty(ERROR_INFORMATION, String.class);
                            variables.put(ERROR_INFORMATION, errorBody);
                            variables.put(ERROR_CODE, exchange.getProperty(ERROR_CODE, String.class));
                            variables.put(ERROR_DESCRIPTION, exchange.getProperty(ERROR_DESCRIPTION, String.class));
                        } else {
                            String serverTransactionId = exchange.getProperty(SERVER_TRANSACTION_ID, String.class);
                            variables.put(TRANSACTION_FAILED, false);
                            variables.put(TRANSFER_CREATE_FAILED, false);
                            variables.put(SERVER_TRANSACTION_ID, serverTransactionId);
                        }
                    }

                    client.newCompleteCommand(job.getKey())
                            .variables(variables)
                            .send()
                            .join();
                })
                .name("init-transfer")
                .maxJobsActive(workerMaxJobs)
                .open();

        zeebeClient.newWorker()
                .jobType("delete-workflow-instancekey")
                .handler(((client, job) -> {
                    logger.info("Removing Workflow Instance key and Mpesa Txn Id from store");
                    Map<String, Object> variables = job.getVariablesAsMap();
                    String mpesaTxnId = variables.get("mpesaTxnId").toString();
                    logger.debug("Txn Id Removed :{}", mpesaTxnId);
                    workflowInstanceStore.remove(mpesaTxnId);
                    client.newCompleteCommand(job.getKey())
                            .send()
                            .join();
                }))
                .name("Cleanup")
                .maxJobsActive(workerMaxJobs)
                .open();
    }
}
