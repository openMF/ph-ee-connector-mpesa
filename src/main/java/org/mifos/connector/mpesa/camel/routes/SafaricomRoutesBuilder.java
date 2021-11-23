package org.mifos.connector.mpesa.camel.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.json.JSONObject;
import org.mifos.connector.mpesa.auth.AccessTokenStore;
import org.mifos.connector.mpesa.dto.BuyGoodsPaymentRequestDTO;
import org.mifos.connector.mpesa.dto.TransactionStatusRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.mifos.connector.mpesa.camel.config.CamelProperties.*;
import static org.mifos.connector.mpesa.safaricom.config.SafaricomProperties.MPESA_BUY_GOODS_TRANSACTION_TYPE;
import static org.mifos.connector.mpesa.utility.SafaricomUtils.getPassword;


@Component
public class SafaricomRoutesBuilder extends RouteBuilder {

    @Value("${mpesa.api.passKey}")
    private String passKey;

    @Value("${mpesa.api.lipana}")
    private String buyGoodsBaseUrl;

    @Value("${mpesa.api.transaction-status}")
    private String transactionStatusBaseUrl;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccessTokenStore accessTokenStore;

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

                    exchange.setProperty(BUY_GOODS_REQUEST_BODY, transactionStatusRequestDTO);
                    logger.info(body);
                })
                .to("direct:lipana-transaction-status");


        /*
           Use this endpoint for receiving the callback form safaricom mpesa endpoint
         */
        from("rest:POST:/buygoods/callback")
                .id("buy-goods-callback")
                .setBody(exchange -> {
                    String body = exchange.getIn().getBody(String.class);
                    JSONObject object = new JSONObject(body);
                    logger.info(body);
                    // TODO IMPLEMENTATION
                    return object.toString();
                });

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
                .to("direct:lipana-buy-goods");

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

                    logger.info(buyGoodsPaymentRequestDTO.toString());
                    logger.info(accessTokenStore.getAccessToken());

                    return buyGoodsPaymentRequestDTO;
                })
                .marshal().json(JsonLibrary.Jackson)
                .toD(buyGoodsBaseUrl+"?bridgeEndpoint=true&throwExceptionOnFailure=false");

        /*
         * Takes the request for transaction status and forwards in to the lipana transaction status endpoint
         */
        from("direct:lipana-transaction-status")
                .removeHeader("*")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader("Content-Type", constant("application/json"))
                .setHeader("Authorization", simple("Bearer ${exchangeProperty."+ACCESS_TOKEN+"}"))
                .setBody(exchange -> {
                    TransactionStatusRequestDTO transactionStatusRequestDTO =
                            (TransactionStatusRequestDTO) exchange.getProperty(BUY_GOODS_TRANSACTION_STATUS_BODY);
                    logger.info(transactionStatusRequestDTO.toString());
                    return  transactionStatusRequestDTO;
                })
                .marshal().json(JsonLibrary.Jackson)
                .toD(transactionStatusBaseUrl+"?bridgeEndpoint=true&throwExceptionOnFailure=false");
    }
}
