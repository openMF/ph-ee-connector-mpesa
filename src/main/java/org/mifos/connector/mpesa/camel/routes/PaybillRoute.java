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

import static org.mifos.connector.mpesa.camel.config.CamelProperties.CHANNEL_REQUEST;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.TRANSACTION_ID;

@Component
public class PaybillRoute extends ErrorHandlerRouteBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private ZeebeClient zeebeClient;
    @Value("${channel.host}")
    private String channelUrl;
    @Value("${currency}")
    private String currency;
    @Value("${accountHoldingInstitutionId}")
    private String accountHoldingInstitutionId;
    @Autowired
    private MpesaUtils mpesaUtils;
    private final String secondaryIdentifierName = "MSISDN";
    //    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private MpesaPaybillProp mpesaPaybillProp;

    @Override
    public void configure() {

        from("rest:POST:/validation")
                .id("mpesa-validation")
                .unmarshal().json(JsonLibrary.Jackson, PaybillRequestDTO.class)
                .log(LoggingLevel.INFO, "## Paybill request payload")
                .setBody(e -> {
                    PaybillRequestDTO paybillRequestDTO = e.getIn().getBody(PaybillRequestDTO.class);
                    //Getting the ams name
                    String businessShortCode = paybillRequestDTO.getShortCode();
                    String amsName = mpesaPaybillProp.getAMSFromShortCode(businessShortCode);
                    String amsUrl = mpesaUtils.getAMSUrl(amsName);

                    e.getIn().setHeader("amsUrl", amsUrl);
                    e.getIn().setHeader("amsName", amsName);
                    e.getIn().setHeader("accountHoldingInstitutionId", accountHoldingInstitutionId);
                    e.setProperty("channelUrl", channelUrl);
                    e.setProperty("secondaryIdentifier", secondaryIdentifierName);
                    e.setProperty("secondaryIdentifierValue", paybillRequestDTO.getMsisdn());
                    ChannelRequestDTO obj = MpesaUtils.convertPaybillPayloadToChannelPayload(paybillRequestDTO, amsName, currency);
                    String channelRequestDTO = null;
                    try {
                        channelRequestDTO = objectMapper.writeValueAsString(obj);
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                    return channelRequestDTO;
                })
                .log("MPESA Request Body : ${body}")
                .toD("${header.channelUrl}" + "/accounts/validate/${header.secondaryIdentifier}/${header.secondaryIdentifierValue}" + "?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .log(LoggingLevel.INFO, "Request sent to channel")
                .setBody(e -> {
                    String paybillResponseBodyString = e.getIn().getBody(String.class);
                    JSONObject paybillResponse = new JSONObject(paybillResponseBodyString);
                    logger.debug("Paybill Response Body : {}", paybillResponseBodyString);
                    logger.debug("Reconciled : {}", paybillResponse.getBoolean("reconciled"));
                    GsmaTransfer gsmaTransfer = mpesaUtils.createGsmaTransferDTO(paybillResponse);
                    e.getIn().setHeader("accountHoldingInstitutionId", paybillResponse.getString("accountHoldingInstitutionId"));
                    e.getIn().setHeader("amsName", paybillResponse.getString("amsName"));
                    e.getIn().setHeader("reconciled", paybillResponse.getBoolean("reconciled"));
                    e.getIn().setHeader("mpesaTxnId", paybillResponse.getString("transactionId"));
                    e.setProperty("channelUrl", channelUrl);
                    return gsmaTransfer.toString();
                })
                .toD("${header.channelUrl}" + "/channel/gsma/transaction}" + "?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .process(e -> {
                    // Setting mpesa specifc response
                    String channelResponseBodyString = e.getIn().getBody(String.class);
                    JSONObject channelResponse = new JSONObject(channelResponseBodyString);
                    String mpesaTxnId = e.getIn().getHeader("mpesaTxnId").toString();
                    Boolean reconciled = Boolean.valueOf(e.getIn().getHeader("reconciled").toString());
                    // Storing in redis
                    String value = channelResponse.getString("transactionId");
                    String key = mpesaTxnId;
//                    redisTemplate.opsForValue().set(key, value);
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
                    String amsUrl = mpesaUtils.getAMSUrl(amsName);

                    e.setProperty("amsUrl", amsUrl);
                    e.setProperty("secondaryIdentifier", "MSISDN");
                    e.setProperty("secondaryIdentifierValue", paybillConfirmationRequestDTO.getMsisdn());

                    ChannelSettlementRequestDTO obj = MpesaUtils.convertPaybillToChannelPayload(paybillConfirmationRequestDTO, amsName, currency);
                    e.setProperty("CONFIRMATION_REQUEST", obj.toString());

                    Map<String, Object> variables = new HashMap<>();
                    variables.put("confirmationReceived", true);
                    variables.put(CHANNEL_REQUEST, obj.toString());
                    variables.put("amount", paybillConfirmationRequestDTO.getTransactionAmount());
                    variables.put("accountId", paybillConfirmationRequestDTO.getBillRefNo());
                    variables.put("originDate", paybillConfirmationRequestDTO.getTransactionTime());
                    variables.put("phoneNumber", paybillConfirmationRequestDTO.getMsisdn());
                    //Getting mpesa and workflow transaction id
                    String mpesaTransactionId = paybillConfirmationRequestDTO.getTransactionID();
                    String transactionId = "";/*redisTemplate.opsForValue().get(mpesaTransactionId);*/

                    logger.debug("Workflow transaction id : {}", transactionId);
                    variables.put("mpesaTransactionId", mpesaTransactionId);
                    variables.put(TRANSACTION_ID, transactionId);

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
