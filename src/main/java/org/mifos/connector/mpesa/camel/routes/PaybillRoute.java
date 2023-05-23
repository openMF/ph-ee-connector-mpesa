package org.mifos.connector.mpesa.camel.routes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.LoggingLevel;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.json.JSONObject;
import org.mifos.connector.common.camel.ErrorHandlerRouteBuilder;
import org.mifos.connector.common.gsma.dto.GsmaTransfer;
import org.mifos.connector.mpesa.dto.ChannelRequestDTO;
import org.mifos.connector.mpesa.dto.ChannelSettlementRequestDTO;
import org.mifos.connector.mpesa.dto.PaybillRequestDTO;
import org.mifos.connector.mpesa.dto.PaybillResponseDTO;
import org.mifos.connector.mpesa.utility.MpesaPaybillProp;
import org.mifos.connector.mpesa.utility.MpesaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.mifos.connector.mpesa.camel.config.CamelProperties.ACCOUNT_HOLDING_INSTITUTION_ID;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.AMS_NAME;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.CHANNEL_REQUEST;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.CLIENT_CORRELATION_ID;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.CONTENT_TYPE;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.CONTENT_TYPE_VAL;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.CUSTOM_HEADER_FILTER_STRATEGY;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.TENANT_ID;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.TRANSACTION_ID;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.TRANSFER_CREATE_FAILED;

@Component
public class PaybillRoute extends ErrorHandlerRouteBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private ZeebeClient zeebeClient;
    @Value("${channel.host}")
    private String channelUrl;
    @Autowired
    private MpesaUtils mpesaUtils;
    private final String secondaryIdentifierName = "MSISDN";
    @Autowired
    private MpesaPaybillProp mpesaPaybillProp;

    public static HashMap<String, Boolean> reconciledStore = new HashMap<>();
    public static HashMap<String, String> workflowInstanceStore = new HashMap<>();

    @Override
    public void configure() {

        from("rest:POST:/validation")
                .id("mpesa-validation")
                .log(LoggingLevel.INFO, "## Paybill validation request")
                .to("direct:account-status")
                .unmarshal().json(JsonLibrary.Jackson, PaybillResponseDTO.class)
                .log("${body.isReconciled}")
                .choice()
                    .when().simple("${body.isReconciled} == 'true'")
                        .to("direct:start-paybill-workflow")
                            .to("direct:paybill-response-success")
                    .otherwise()
                        .to("direct:paybill-response-failure")
                .end();

        from("direct:start-paybill-workflow")
                .id("start-paybill-workflow")
                .log(LoggingLevel.INFO, "Starting GSMA Txn Workflow for Paybill")
                .setBody(e -> {
                    String gsmaTransferDTO = null;
                    PaybillResponseDTO paybillResponseDTO = e.getIn().getBody(PaybillResponseDTO.class);
                    logger.debug("Paybill Response :{}", paybillResponseDTO.toString());
                    logger.debug("Reconciled : {}", paybillResponseDTO.isReconciled());

                    Boolean reconciled = paybillResponseDTO.isReconciled();
                    String mpesaTxnId = paybillResponseDTO.getTransactionId();
                    String clientCorrelationId = mpesaTxnId;
                    reconciledStore.put(clientCorrelationId, reconciled);
                    GsmaTransfer gsmaTransfer = mpesaUtils.createGsmaTransferDTO(paybillResponseDTO,clientCorrelationId);
                    e.getIn().removeHeaders("*");
                    e.getIn().setHeader(ACCOUNT_HOLDING_INSTITUTION_ID, paybillResponseDTO.getAccountHoldingInstitutionId());
                    e.getIn().setHeader(AMS_NAME, paybillResponseDTO.getAmsName());
                    e.getIn().setHeader(TENANT_ID, paybillResponseDTO.getAccountHoldingInstitutionId());
                    e.getIn().setHeader(CLIENT_CORRELATION_ID, clientCorrelationId);
                    e.getIn().setHeader(CONTENT_TYPE, CONTENT_TYPE_VAL);
                    e.setProperty("channelUrl", channelUrl);
                    try {
                        gsmaTransferDTO = objectMapper.writeValueAsString(gsmaTransfer);
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                    return gsmaTransferDTO;
                })
                .toD("${header.channelUrl}" + "/channel/gsma/transaction" + "?bridgeEndpoint=true&throwExceptionOnFailure=false" +
                        "&headerFilterStrategy=#" + CUSTOM_HEADER_FILTER_STRATEGY)
                .log(LoggingLevel.INFO, "Starting GSMA Txn Workflow in channel");

        from("direct:account-status")
                .id("account-status-channel")
                .unmarshal().json(JsonLibrary.Jackson, PaybillRequestDTO.class)
                .log(LoggingLevel.INFO, "Paybill Validation Payload")
                .setBody(exchange -> {
                    PaybillRequestDTO paybillRequestDTO = exchange.getIn().getBody(PaybillRequestDTO.class);
                    //Getting the ams name
                    String businessShortCode = paybillRequestDTO.getShortCode();
                    String amsName = mpesaPaybillProp.getAMSFromShortCode(businessShortCode);
                    String currency = mpesaPaybillProp.getCurrencyFromShortCode(businessShortCode);
                    String amsUrl = mpesaUtils.getAMSUrl(amsName);
                    String accountHoldingInstitutionId = mpesaPaybillProp.getAccountHoldingInstitutionId();
                    exchange.getIn().removeHeaders("*");
                    exchange.getIn().setHeader("amsUrl", amsUrl);
                    exchange.getIn().setHeader(CONTENT_TYPE, CONTENT_TYPE_VAL);
                    exchange.getIn().setHeader("amsName", amsName);
                    exchange.getIn().setHeader("accountHoldingInstitutionId", accountHoldingInstitutionId);
                    exchange.setProperty("channelUrl", channelUrl);
                    exchange.setProperty("secondaryIdentifier", secondaryIdentifierName);
                    exchange.setProperty("secondaryIdentifierValue", paybillRequestDTO.getMsisdn());
                    ChannelRequestDTO obj = MpesaUtils.convertPaybillPayloadToChannelPayload(paybillRequestDTO, amsName, currency);
                    logger.debug("Header:{}", exchange.getIn().getHeaders());
                    try {
                        return objectMapper.writeValueAsString(obj);
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .toD("${header.channelUrl}" + "/accounts/validate/${header.secondaryIdentifier}/${header.secondaryIdentifierValue}" + "?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .log(LoggingLevel.INFO, "Account Status request sent to channel");

        from("direct:paybill-response-success")
                .id("paybill-response-success")
                .log(LoggingLevel.INFO, "Sending paybill response")
                .process(e -> {
                    // Setting mpesa specifc response
                    String channelResponseBodyString = e.getIn().getBody(String.class);
                    logger.debug("channelResponseBodyString:{}", channelResponseBodyString);
                    JSONObject channelResponse = new JSONObject(channelResponseBodyString);
                    String workflowInstanceKey = channelResponse.getString("transactionId");

                    String clientCorrelationId = e.getIn().getHeader(CLIENT_CORRELATION_ID).toString();
                    Boolean reconciled = reconciledStore.get(clientCorrelationId);
                    // Storing the key value
                    workflowInstanceStore.put(clientCorrelationId, workflowInstanceKey);
                    reconciledStore.remove(clientCorrelationId);
                    JSONObject responseObject = new JSONObject();
                    responseObject.put("ResultCode", reconciled ? 0 : 1);
                    responseObject.put("ResultDesc", reconciled ? "Accepted" : "Rejected");
                    e.getIn().setBody(responseObject.toString());
                });

        from("direct:paybill-response-failure")
                .id("paybill-response-failure")
                .log(LoggingLevel.INFO, "Sending paybill response")
                .process(e -> {
                    // Setting mpesa specifc response
                    String channelResponseBodyString = e.getIn().getBody(String.class);
                    logger.debug("channelResponseBodyString:{}", channelResponseBodyString);
                    Boolean reconciled = false;
                    JSONObject responseObject = new JSONObject();
                    responseObject.put("ResultCode", reconciled ? 0 : 1);
                    responseObject.put("ResultDesc", reconciled ? "Accepted" : "Rejected");
                    e.getIn().setBody(responseObject.toString());
                });

        from("rest:POST:/confirmation")
                .id("mpesa-confirmation")
                .unmarshal().json(JsonLibrary.Jackson, PaybillRequestDTO.class)
                .log(LoggingLevel.INFO, "Setting zeebe variable for confirmation")
                .process(e -> {
                    PaybillRequestDTO paybillConfirmationRequestDTO = e.getIn().getBody(PaybillRequestDTO.class);
                    e.setProperty("mpesaTransactionId", paybillConfirmationRequestDTO.getTransactionID());
                    //Getting the ams name
                    String businessShortCode = paybillConfirmationRequestDTO.getShortCode();
                    String amsName = mpesaPaybillProp.getAMSFromShortCode(businessShortCode);
                    String currency = mpesaPaybillProp.getCurrencyFromShortCode(businessShortCode);
                    String amsUrl = mpesaUtils.getAMSUrl(amsName);

                    e.setProperty("amsUrl", amsUrl);
                    e.setProperty("secondaryIdentifier", "MSISDN");
                    e.setProperty("secondaryIdentifierValue", paybillConfirmationRequestDTO.getMsisdn());

                    ChannelSettlementRequestDTO obj = mpesaUtils.convertPaybillToChannelPayload(paybillConfirmationRequestDTO, amsName, currency);
                    e.setProperty("CONFIRMATION_REQUEST", obj.toString());
                    //Getting mpesa and workflow transaction id and removing key
                    String mpesaTransactionId = paybillConfirmationRequestDTO.getTransactionID();
                    String transactionId = workflowInstanceStore.get(mpesaTransactionId);
                    workflowInstanceStore.remove(mpesaTransactionId);

                    Map<String, Object> variables = new HashMap<>();
                    variables.put("confirmationReceived", true);
                    variables.put(CHANNEL_REQUEST, obj.toString());
                    variables.put("amount", paybillConfirmationRequestDTO.getTransactionAmount());
                    variables.put("accountId", paybillConfirmationRequestDTO.getBillRefNo());
                    variables.put("originDate", Long.parseLong(paybillConfirmationRequestDTO.getTransactionTime()));
                    variables.put("phoneNumber", paybillConfirmationRequestDTO.getMsisdn());
                    variables.put("mpesaTransactionId", mpesaTransactionId);
                    variables.put(TRANSACTION_ID, transactionId);
                    variables.put(TRANSFER_CREATE_FAILED, false);
                    logger.info("Workflow transaction id : {}", transactionId);

                    if (transactionId != null) {
                        zeebeClient.newPublishMessageCommand()
                                .messageName("pendingConfirmation")
                                .correlationKey(transactionId)
                                .timeToLive(Duration.ofMillis(300))
                                .variables(variables)
                                .send();
                        logger.debug("Published Variables");
                    } else {
                        logger.debug("No workflow of such transaction ID exists");
                    }
                });
    }
}
