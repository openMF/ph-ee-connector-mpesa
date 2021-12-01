package org.mifos.connector.mpesa.camel.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.util.json.JsonObject;
import org.json.JSONObject;
import org.mifos.connector.common.gsma.dto.RequestStateDTO;
import org.mifos.connector.mpesa.auth.AccessTokenStore;
import org.mifos.connector.mpesa.dto.BuyGoodsPaymentRequestDTO;
import org.mifos.connector.mpesa.dto.TransactionStatusRequestDTO;
import org.mifos.connector.mpesa.flowcomponents.CorrelationIDStore;
import org.mifos.connector.mpesa.flowcomponents.transaction.CollectionResponseProcessor;
import org.mifos.connector.mpesa.flowcomponents.transaction.TransactionResponseProcessor;
import org.mifos.connector.mpesa.utility.SafaricomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.*;
import static org.mifos.connector.mpesa.safaricom.config.SafaricomProperties.MPESA_BUY_GOODS_TRANSACTION_TYPE;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.*;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.TRANSACTION_ID;


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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CollectionResponseProcessor collectionResponseProcessor;

    @Autowired
    private TransactionResponseProcessor transactionResponseProcessor;

    @Autowired
    private AccessTokenStore accessTokenStore;

    @Autowired
    private CorrelationIDStore correlationIDStore;

    @Autowired
    private SafaricomUtils safaricomUtils;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void configure() {

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

        /*
         * Starts the payment flow
         *
         * Step1: Authenticate the user by initiating [get-access-token] flow
         * Step2: On successful [Step1], directs to [lipana-buy-goods] flow
         */
        from("direct:get-transaction-status-base")
                .id("buy-goods-get-transaction-status-base")
                .log(LoggingLevel.INFO, "Starting buy goods flow")
                .to("direct:get-access-token")
                .process(exchange -> {
                    exchange.setProperty(ACCESS_TOKEN, accessTokenStore.getAccessToken());
                })
                .log(LoggingLevel.INFO, "Got access token, moving on to API call.")
                .to("direct:lipana-transaction-status")
                .log(LoggingLevel.INFO, "Status: ${header.CamelHttpResponseCode}")
                .log(LoggingLevel.INFO, "Transaction API response: ${body}")
                .to("direct:transaction-status-response-handler");

        /*
         * Route to handle async transaction status API responses
         */
        from("direct:transaction-status-response-handler")
                .id("transaction-status-response-handler")
                .choice()
                .when(header("CamelHttpResponseCode").isEqualTo("200"))
                .log(LoggingLevel.INFO, "Collection request successful")
                .process(exchange -> {

                    JSONObject jsonObject = new JSONObject(exchange.getIn().getBody(String.class));
                    String server_id = jsonObject.getString("CheckoutRequestID");
                    String resultCode = jsonObject.getString("ResultCode");
                    Object correlationId = exchange.getProperty(CORRELATION_ID);

                    if(resultCode.equals("0")) {
                        exchange.setProperty(TRANSACTION_FAILED, false);
                    } else {
                        exchange.setProperty(TRANSACTION_FAILED, true);
                    }

                    exchange.setProperty(SERVER_TRANSACTION_ID, server_id);
                    exchange.setProperty(TRANSACTION_ID, correlationId);

                })
                .process(collectionResponseProcessor)
                .otherwise()
                .log(LoggingLevel.ERROR, "Collection request unsuccessful")
                .process(exchange -> {
                    Object correlationId = exchange.getProperty(CORRELATION_ID);
                    exchange.setProperty(TRANSACTION_ID, correlationId);
                })
                .setProperty(TRANSACTION_FAILED, constant(true))
                .process(collectionResponseProcessor);

        /*
         * Route to handle async API responses
         */
        from("direct:transaction-response-handler")
                .id("transaction-response-handler")
                .choice()
                .when(header("CamelHttpResponseCode").isEqualTo("200"))
                .log(LoggingLevel.INFO, "Collection request successful")
                .process(exchange -> {

                    JSONObject jsonObject = new JSONObject(exchange.getIn().getBody(String.class));
                    String server_id = jsonObject.getString("CheckoutRequestID");
                    Object correlationId = exchange.getProperty(CORRELATION_ID);

                    exchange.setProperty(SERVER_TRANSACTION_ID, server_id);
                    exchange.setProperty(TRANSACTION_ID, correlationId);

                })
                .process(transactionResponseProcessor)
                .otherwise()
                .log(LoggingLevel.ERROR, "Collection request unsuccessful")
                .process(exchange -> {
                    Object correlationId = exchange.getProperty(CORRELATION_ID);
                    exchange.setProperty(TRANSACTION_ID, correlationId);
                })
                .setProperty(TRANSACTION_FAILED, constant(true))
                .process(transactionResponseProcessor);

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


                    String password = safaricomUtils.getPassword("" + buyGoodsPaymentRequestDTO.getBusinessShortCode(),
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
                .log(LoggingLevel.INFO, "MPESA API called, response: \n\n..\n\n..\n\n.. ${body}");

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

                    transactionStatusRequestDTO.setBusinessShortCode(buyGoodsPaymentRequestDTO.getBusinessShortCode());
                    transactionStatusRequestDTO.setTimestamp(""+safaricomUtils.getTimestamp());
                    transactionStatusRequestDTO.setCheckoutRequestId(
                            exchange.getProperty(SERVER_TRANSACTION_ID, String.class));
                    transactionStatusRequestDTO.setPassword(safaricomUtils.getPassword(
                            "" + transactionStatusRequestDTO.getBusinessShortCode(),
                            passKey, transactionStatusRequestDTO.getTimestamp()
                    ));


                    logger.info("TRANSACTION STATUS REQUEST DTO \n\n..\n\n..\n\n.." + transactionStatusRequestDTO.toString());
                    return  transactionStatusRequestDTO;
                })
                .marshal().json(JsonLibrary.Jackson)
                .toD(buyGoodsHost + transactionStatusUrl +"?bridgeEndpoint=true&throwExceptionOnFailure=false")
                .log(LoggingLevel.INFO, "MPESA STATUS called, response: \n\n..\n\n..\n\n.. ${body}");
    }
}
