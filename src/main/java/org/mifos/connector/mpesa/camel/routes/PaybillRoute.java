package org.mifos.connector.mpesa.camel.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.LoggingLevel;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.json.JSONObject;
import org.mifos.connector.common.camel.ErrorHandlerRouteBuilder;
import org.mifos.connector.mpesa.dto.ChannelRequestDTO;
import org.mifos.connector.mpesa.dto.PaybillRequestDTO;
import org.mifos.connector.mpesa.utility.MpesaUtils;
import org.mifos.connector.mpesa.zeebe.ZeebeProcessStarter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class PaybillRoute extends ErrorHandlerRouteBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper objectMapper;
    @Autowired
    private ZeebeProcessStarter zeebeProcessStarter;
    @Autowired
    private ZeebeClient zeebeClient;
    @Value("${channel.host}")
    private String channelUrl;

    private final String secondaryIdentifierName = "MSISDN";
    @Value("${timer}")
    private String timer;
    private static HashMap<String, String> hm = new HashMap<>();

    public PaybillRoute(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void configure() {
        from("rest:POST:/validation")
                .id("mpesa-validation")
                .unmarshal().json(JsonLibrary.Jackson, PaybillRequestDTO.class)
                .log(LoggingLevel.INFO, "## Paybill request payload")
                .setBody(e -> {
                    PaybillRequestDTO paybillRequestDTO = e.getIn().getBody(PaybillRequestDTO.class);
                    //Getting the ams name
                    String amsName = e.getIn().getHeader("amsName", String.class);
                    logger.info("AMS Name : {}", amsName);
                    //Channel URL
                    logger.info("Channel URL : {}", channelUrl);
                    e.setProperty("channelUrl", channelUrl);
                    e.setProperty("secondaryIdentifier", secondaryIdentifierName);
                    e.setProperty("secondaryIdentifierValue", paybillRequestDTO.getMsisdn());
                    ChannelRequestDTO obj = MpesaUtils.convertPaybillPayloadToChannelPayload(paybillRequestDTO, amsName);
                    logger.info("Paybill Object:{}", obj);
                    return obj.toString();
                })
                .log("MPESA Request Body : ${body}")
                .toD("${header.channelUrl}" + "/accounts/validate/${header.secondaryIdentifier}/${header.secondaryIdentifierValue}" + "?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .choice()
                .when(header("CamelHttpResponseCode").isEqualTo("200"))
                .log(LoggingLevel.INFO, "Request sent to channel")
                .process(e -> {
                    String paybillRequestBodyString = e.getIn().getBody(String.class);
                    JSONObject paybillRequest = new JSONObject(paybillRequestBodyString);
                    logger.info("Paybill Request Body : {}", paybillRequestBodyString);
                    logger.info("Reconciled : {}", paybillRequest.getBoolean("reconciled"));
                    e.setProperty("MPESA_VALIDATION_WEBHOOK_SUCCESS", paybillRequest.getBoolean("reconciled"));

                    Map<String, Object> variables = new HashMap<>();
                    variables.put("timer", timer);
                    variables.put("paybillRequestBody", paybillRequestBodyString);
                    variables.put("validationFailed", !(paybillRequest.getBoolean("reconciled")));
                    variables.put("confirmationReceived", false);
                    //Starting the paybill workflow
                    String transactionId = zeebeProcessStarter.startZeebeWorkflowPaybill("paybill", variables);

                    String mpesaTransactionId = paybillRequest.getString("transaction_id");
                    // Storing the external txn id and workflow txn id
                    logger.debug("MPESA Txn ID :{}", mpesaTransactionId);
                    logger.debug("Txn ID :{}", transactionId);
                    hm.put(mpesaTransactionId, transactionId);
                })
                .otherwise()
                .log(LoggingLevel.INFO, "Request failed to sent to channel")
                .process(e -> {
                    e.setProperty("MPESA_VALIDATION_WEBHOOK_SUCCESS", false);
                    e.setProperty("ERROR_DESCRIPTION", e.getIn().getBody(String.class));
                    e.setProperty("ERROR_CODE", e.getIn().getHeader(e.HTTP_RESPONSE_CODE));
                });

        from("rest:POST:/confirmation")
                .id("mpesa-confirmation")
                .unmarshal().json(JsonLibrary.Jackson, PaybillRequestDTO.class)
                .log(LoggingLevel.INFO, "Setting zeebe variable for confirmation")
                .process(e -> {
                    PaybillRequestDTO paybillConfirmationRequestDTO = e.getIn().getBody(PaybillRequestDTO.class);
                    e.setProperty("mpesaTransactionId", paybillConfirmationRequestDTO.getTransactionID());
                    //Getting the ams name
                    String amsName = e.getIn().getHeader("amsName", String.class);
                    logger.info("AMS Name : {}", amsName);
                    String amsUrl = MpesaUtils.getAMSUrl(amsName);
                    e.setProperty("amsUrl", amsUrl);
                    e.setProperty("secondaryIdentifier", "MSISDN");
                    e.setProperty("secondaryIdentifierValue", paybillConfirmationRequestDTO.getMsisdn());
                    ChannelRequestDTO obj = MpesaUtils.convertPaybillPayloadToChannelPayload(paybillConfirmationRequestDTO, amsName);
                    e.setProperty("PAYBILL_CONFIRMATION_REQUEST", obj);

                    Map<String, Object> variables = new HashMap<>();
                    variables.put("confirmationReceived", true);
                    variables.put("CHANNEL_REQUEST", e.getProperty("PAYBILL_CONFIRMATION_REQUEST"));
                    //Getting mpesa and workflow transaction id
                    String mpesaTransactionId = e.getProperty("mpesaTransactionId").toString();
                    String transactionId = hm.get(mpesaTransactionId);
                    logger.debug("Workflow transaction id : {}", transactionId);
                    variables.put("mpesaTransactionId", mpesaTransactionId);

                    if (transactionId != null) {
                        zeebeClient.newPublishMessageCommand()
                                .messageName("pendingConfirmation")
                                .correlationKey((String) transactionId)
                                .timeToLive(Duration.ofMillis(300))
                                .variables(variables)
                                .send();
                        logger.info("Published Variables");
                    } else {
                        logger.debug("No workflow of such transaction ID exists");
                    }

                });
    }
}
