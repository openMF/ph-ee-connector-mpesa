package org.mifos.connector.mpesa.camel.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.util.json.JsonObject;
import org.mifos.connector.common.gsma.dto.RequestStateDTO;
import org.mifos.connector.mpesa.auth.AccessTokenStore;
import org.mifos.connector.mpesa.dto.BuyGoodsPaymentRequestDTO;
import org.mifos.connector.mpesa.dto.TransactionStatusRequestDTO;
import org.mifos.connector.mpesa.flowcomponents.CorrelationIDStore;
import org.mifos.connector.mpesa.flowcomponents.transaction.CollectionResponseProcessor;
import org.mifos.connector.mpesa.utility.SafaricomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.mifos.connector.mpesa.camel.config.CamelProperties.*;
import static org.mifos.connector.mpesa.safaricom.config.SafaricomProperties.MPESA_BUY_GOODS_TRANSACTION_TYPE;
import static org.mifos.connector.mpesa.utility.SafaricomUtils.getPassword;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.TRANSACTION_FAILED;


@Component
public class SafaricomRoutesBuilder extends RouteBuilder {

    @Value("${mpesa.api.pass-key}")
    private String passKey;

    @Value("${mpesa.api.host}")
    private String buyGoodsHost;

    @Value("${mpesa.api.lipana}")
    private String buyGoodsLipanaUrl;

    @Value("${mpesa.api.transaction-status}")
    private String transactionStatusUrl;

    @Value("${mpesa.local.host}")
    private String localhost;

    @Value("${mpesa.local.queue-timeout-url}")
    private String queueTimeoutEndpoint;

    @Value("${mpesa.local.result-url}")
    private String resultUrlEndpoint;

    @Value("${mpesa.initiator.name}")
    private String initiatorName;

    @Value("${mpesa.initiator.security-credentials}")
    private String securityCredentials;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CollectionResponseProcessor collectionResponseProcessor;

    @Autowired
    private AccessTokenStore accessTokenStore;

    @Autowired
    private CorrelationIDStore correlationIDStore;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void configure() {

        from("rest:POST:/buygoods/queuetimeout/callback")
                .id("queue-timeout-url")
                .log("Queue time url body ..\n..\n..\n..\n ${body}");

        from("rest:POST:/buygoods/result/callback")
                .id("result-url")
                .log("Result url body ..\n..\n..\n..\n ${body}");

        /*
         * Use this endpoint for getting the mpesa transaction status
         * The request parameter is same as the safaricom standards
         */
        from("rest:POST:/buygoods/transactionstatus")
                .id("buy-goods-transaction-status")
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    TransactionStatusRequestDTO transactionStatusRequestDTO = objectMapper.readValue(
                            body, TransactionStatusRequestDTO.class);

                    exchange.setProperty(BUY_GOODS_TRANSACTION_STATUS_BODY, transactionStatusRequestDTO);
                    logger.info(body);
                })
                .to("direct:lipana-transaction-status");


        /*
           Use this endpoint for receiving the callback form safaricom mpesa endpoint
         */
        from("rest:POST:/buygoods/callback")
                .id("buy-goods-callback")
                .log(LoggingLevel.INFO, "Callback body \n\n..\n\n..\n\n.. ${body}")
                .unmarshal().json(JsonLibrary.Jackson, JsonObject.class)
                .process(exchange -> {
                    JsonObject callback = exchange.getIn().getBody(JsonObject.class);
                    String serverUUID = SafaricomUtils.getTransactionId(callback);
                    correlationIDStore.addMapping(serverUUID,
                            exchange.getProperty(CORRELATION_ID, String.class));
                    exchange.setProperty(TRANSACTION_ID, correlationIDStore.getClientCorrelation(serverUUID));
                    exchange.setProperty(SERVER_TRANSACTION_ID, serverUUID);
                })
                .choice()
                .when(exchange -> exchange.getIn().getBody(RequestStateDTO.class).getStatus().equals("completed"))
                .setProperty(TRANSACTION_FAILED, constant(false))
                .otherwise()
                .setProperty(TRANSACTION_FAILED, constant(true))
                .end()
                .process(collectionResponseProcessor);

        /*
          Rest endpoint to initiate payment for buy goods

          Sample request body: {
              "BusinessShortCode": 174379,
              "Amount": 1,
              "PartyA": 254708374149,
              "PartyB": 174379,
              "PhoneNumber": 254708374149,
              "CallBackURL": "https://mydomain.com/path",
              "AccountReference": "CompanyXLTD",
              "TransactionDesc": "Payment of X"
            }
         */
        from("rest:POST:/buygoods")
                .id("buy-goods-online")
                .process(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    BuyGoodsPaymentRequestDTO buyGoodsPaymentRequestDTO = objectMapper.readValue(
                            body, BuyGoodsPaymentRequestDTO.class);

                    exchange.setProperty(BUY_GOODS_REQUEST_BODY, buyGoodsPaymentRequestDTO);
                    logger.info(body);
                    logger.info(buyGoodsPaymentRequestDTO.toString());

                })
                .to("direct:buy-goods-base");

        /*
         * Starts the payment flow
         *
         * Step1: Authenticate the user by initiating [get-access-token] flow
         * Step2: On successful [Step1], directs to [lipana-buy-goods] flow
         */
        from("direct:buy-goods-base")
                .id("buy-goods-base")
                .log(LoggingLevel.INFO, "Starting buy goods flow")
                .to("direct:get-access-token")
                .process(exchange -> {
                    exchange.setProperty(ACCESS_TOKEN, accessTokenStore.getAccessToken());
                })
                .log(LoggingLevel.INFO, "Got access token, moving on to API call.")
                .to("direct:lipana-buy-goods")
                .log(LoggingLevel.INFO, "Status: ${header.CamelHttpResponseCode}")
                .log(LoggingLevel.INFO, "Transaction API response: ${body}")
                .to("direct:transaction-response-handler");

        /**
         * Route to handle async API responses
         */
        from("direct:transaction-response-handler")
                .id("transaction-response-handler")
                .choice()
                .when(header("CamelHttpResponseCode").isEqualTo("200"))
                .log(LoggingLevel.INFO, "Collection request successful")
                .otherwise()
                .log(LoggingLevel.ERROR, "Collection request unsuccessful")
                .process(exchange -> {
                    Object correlationId = exchange.getProperty(CORRELATION_ID);
                    exchange.setProperty(TRANSACTION_ID, correlationId);
                })
                .setProperty(TRANSACTION_FAILED, constant(true))
                .process(collectionResponseProcessor);

        /*
         * Takes the access toke and payment request and forwards the requests to lipana API.
         * [Password] and [TransactionType] are set in runtime and request is forwarded to lipana endpoint.
         */
        from("direct:lipana-buy-goods")
                .removeHeader("*")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .setHeader("Authorization", simple("Bearer ${exchangeProperty."+ACCESS_TOKEN+"}"))
                .setBody(exchange -> {
                    BuyGoodsPaymentRequestDTO buyGoodsPaymentRequestDTO =
                            (BuyGoodsPaymentRequestDTO) exchange.getProperty(BUY_GOODS_REQUEST_BODY);


                    String password = getPassword("" + buyGoodsPaymentRequestDTO.getBusinessShortCode(),
                            passKey,
                            "" + buyGoodsPaymentRequestDTO.getTimestamp());

                    buyGoodsPaymentRequestDTO.setPassword(password);
                    buyGoodsPaymentRequestDTO.setTransactionType(MPESA_BUY_GOODS_TRANSACTION_TYPE);

                    logger.info("BUY GOODS BODY: \n\n..\n\n..\n\n.. " + buyGoodsPaymentRequestDTO.toString());
                    logger.info(accessTokenStore.getAccessToken());

                    return buyGoodsPaymentRequestDTO;
                })
                .marshal().json(JsonLibrary.Jackson)
                .toD(buyGoodsHost + buyGoodsLipanaUrl +"?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .log(LoggingLevel.INFO, "MPESA API called, response: \n\n..\n\n..\n\n.. ${body}");;

        /*
         * Takes the request for transaction status and forwards in to the lipana transaction status endpoint
         */
        from("direct:lipana-transaction-status")
                .removeHeader("*")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .setHeader("Authorization", simple("Bearer ${exchangeProperty."+ACCESS_TOKEN+"}"))
                .setBody(exchange -> {
                    TransactionStatusRequestDTO transactionStatusRequestDTO = new TransactionStatusRequestDTO();

                    BuyGoodsPaymentRequestDTO buyGoodsPaymentRequestDTO =
                            (BuyGoodsPaymentRequestDTO) exchange.getProperty(BUY_GOODS_REQUEST_BODY);

                    logger.info("BUY GOODS REQUEST: \n\n..\n\n..\n\n.." + buyGoodsPaymentRequestDTO);

                    transactionStatusRequestDTO.setTransactionId(exchange.getProperty(SERVER_TRANSACTION_ID, String.class));
                    transactionStatusRequestDTO.setOccasion("confirming transaction");
                    transactionStatusRequestDTO.setRemarks("get transaction status");
                    transactionStatusRequestDTO.setIdentifierType("4");
                    transactionStatusRequestDTO.setPartyA(""+buyGoodsPaymentRequestDTO.getBusinessShortCode());
                    transactionStatusRequestDTO.setCommandId("TransactionStatusQuery");
                    transactionStatusRequestDTO.setQueueTimeOutUrl("https://3ea9-103-85-119-3.ngrok.io"+queueTimeoutEndpoint);
                    transactionStatusRequestDTO.setResultUrl("https://3ea9-103-85-119-3.ngrok.io"+resultUrlEndpoint);
                    transactionStatusRequestDTO.setInitiator(initiatorName);
                    transactionStatusRequestDTO.setSecurityCredential(securityCredentials);


                    logger.info("TRANSACTION STATUS REQUEST DTO \n\n..\n\n..\n\n.." + transactionStatusRequestDTO.toString());
                    return  transactionStatusRequestDTO;
                })
                .marshal().json(JsonLibrary.Jackson)
                .toD(buyGoodsHost + transactionStatusUrl +"?bridgeEndpoint=true&throwExceptionOnFailure=false");
    }
}
