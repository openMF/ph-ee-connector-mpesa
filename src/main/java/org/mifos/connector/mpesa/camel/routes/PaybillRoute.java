package org.mifos.connector.mpesa.camel.routes;

import io.camunda.zeebe.client.ZeebeClient;
import org.apache.camel.LoggingLevel;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.mifos.connector.common.camel.ErrorHandlerRouteBuilder;
import org.mifos.connector.mpesa.dto.ChannelRequestDTO;
import org.mifos.connector.mpesa.dto.ChannelSettlementRequestDTO;
import org.mifos.connector.mpesa.dto.PaybillRequestDTO;
import org.mifos.connector.mpesa.utility.MpesaPaybillProp;
import org.mifos.connector.mpesa.utility.MpesaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.mifos.connector.mpesa.camel.config.CamelProperties.CHANNEL_REQUEST;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.TRANSACTION_ID;

@Component
public class PaybillRoute extends ErrorHandlerRouteBuilder {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private ZeebeClient zeebeClient;
    @Value("${channel.host}")
    private String channelUrl;
    private final String secondaryIdentifierName = "MSISDN";
    @Value("${timer}")
    private String timer;
    private static HashMap<String, String> hm = new HashMap<>();
    private final MpesaUtils mpesaUtils;
    private RedisTemplate<String, String> redisTemplate;

    public PaybillRoute(String channelUrl, String timer, MpesaUtils mpesaUtils) {
        this.channelUrl = channelUrl;
        this.timer = timer;
        this.mpesaUtils = mpesaUtils;
    }

    @Override
    public void configure() {

        from("direct:redis-store")
                .process(exchange -> {
                    String key = exchange.getIn().getHeader("key", String.class);
                    String value = exchange.getIn().getBody(String.class);
                    redisTemplate.opsForValue().set(key, value);
                });
        // TODO configurable host and port
        from("direct:redist-get")
                .process(exchange -> {
                    String key = exchange.getIn().getHeader("key", String.class);
                    String value = redisTemplate.opsForValue().get(key);
                    exchange.getIn().setBody(value);
                });

        from("rest:POST:/validation")
                .id("mpesa-validation")
                .unmarshal().json(JsonLibrary.Jackson, PaybillRequestDTO.class)
                .log(LoggingLevel.INFO, "## Paybill request payload")
                .setBody(e -> {
                    PaybillRequestDTO paybillRequestDTO = e.getIn().getBody(PaybillRequestDTO.class);
                    //Getting the ams name
                    String businessShortCode = paybillRequestDTO.getShortCode();
                    String amsName = getAMSName(businessShortCode);
                    String currency = MpesaPaybillProp.getCurrency();
                    String accountHoldingInstitutionId = MpesaPaybillProp.getAccountHoldingInstitutionId();
                    String amsUrl = MpesaUtils.getAMSUrl(amsName);

                    e.getIn().setHeader("amsUrl", amsUrl);
                    e.getIn().setHeader("amsName", amsName);
                    e.getIn().setHeader("accountHoldingInstitutionId", accountHoldingInstitutionId);
                    e.getIn().setHeader("X-Account-Holding-Institution-Identifier", paybillRequestDTO.getTransactionID());
                    e.setProperty("channelUrl", channelUrl);

                    ChannelRequestDTO obj = MpesaUtils.convertPaybillPayloadToChannelPayload(paybillRequestDTO, amsName, currency);
                    return obj.toString();
                })
                .log("MPESA Request Body : ${body}")
                .toD("${header.channelUrl}" + "/api/v1/transaction" + "?bridgeEndpoint=true&throwExceptionOnFailure=false");

        from("rest:POST:/confirmation")
                .id("mpesa-confirmation")
                .unmarshal().json(JsonLibrary.Jackson, PaybillRequestDTO.class)
                .log(LoggingLevel.INFO, "Setting zeebe variable for confirmation")
                .process(e -> {
                    PaybillRequestDTO paybillConfirmationRequestDTO = e.getIn().getBody(PaybillRequestDTO.class);
                    e.setProperty("mpesaTransactionId", paybillConfirmationRequestDTO.getTransactionID());
                    //Getting the ams name
                    String businessShortCode = paybillConfirmationRequestDTO.getShortCode();
                    String amsName = getAMSName(businessShortCode);
                    String currency = MpesaPaybillProp.getCurrency();
                    String amsUrl = MpesaUtils.getAMSUrl(amsName);
                    e.setProperty("amsUrl", amsUrl);

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
                    String mpesaTransactionId = e.getProperty("mpesaTransactionId").toString();
                    String transactionId = hm.get(mpesaTransactionId);
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

    private String getAMSName(String businessShortCode) {
        String amsValue = "null";
        for (MpesaPaybillProp.ShortCode shortCode : MpesaUtils.postConstruct()) {
            if (businessShortCode.equalsIgnoreCase(shortCode.getBusinessShortCode())) {
                amsValue = shortCode.getAms();
            } else {
                amsValue = shortCode.getDefaultAms();
            }
        }
    }
}
