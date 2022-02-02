package org.mifos.connector.mpesa.utility;

import org.apache.camel.util.json.JsonObject;
import org.mifos.connector.common.channel.dto.TransactionChannelCollectionRequestDTO;
import org.mifos.connector.common.gsma.dto.GsmaParty;
import org.mifos.connector.mpesa.dto.BuyGoodsPaymentRequestDTO;
import org.mifos.connector.mpesa.dto.StkCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicReference;
import static org.mifos.connector.mpesa.safaricom.config.SafaricomProperties.MPESA_BUY_GOODS_TRANSACTION_TYPE;

@Component
public class SafaricomUtils {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${mpesa.local.host}")
    private String host;

    @Value("${mpesa.local.transaction-callback}")
    private String callbackEndpoint;

    @Value("${mpesa.api.pass-key}")
    private String passKey;

    @Value("${mpesa.business-short-code}")
    private Long businessShortCode;

    @Value("${mpesa.till}")
    private Long tillNumber;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    public BuyGoodsPaymentRequestDTO channelRequestConvertor(TransactionChannelCollectionRequestDTO transactionChannelRequestDTO) {
        logger.info("TransactionChannelCollectionRequestDTO chile converting " + transactionChannelRequestDTO);
        BuyGoodsPaymentRequestDTO buyGoodsPaymentRequestDTO = new BuyGoodsPaymentRequestDTO();


        long amount = Long.parseLong(transactionChannelRequestDTO.getAmount().getAmount());
        long timestamp = getTimestamp(); //123; //Long.parseLong(sdf.format(new Date()));
        long payer;

        GsmaParty[] party = transactionChannelRequestDTO.getPayer();

        if (party[0].getKey().equals("MSISDN")) {
            // case where 1st array element is MSISDN
            payer = Long.parseLong(party[0].getValue());
        } else {
            // case where 1st array element is ACCOUNTID
            payer = Long.parseLong(party[1].getValue());
        }

        buyGoodsPaymentRequestDTO.setTimestamp(""+timestamp);
        buyGoodsPaymentRequestDTO.setCallBackURL(host + callbackEndpoint);

        buyGoodsPaymentRequestDTO.setPartyA(payer);
        buyGoodsPaymentRequestDTO.setPhoneNumber(payer);

        buyGoodsPaymentRequestDTO.setPartyB(tillNumber);
        buyGoodsPaymentRequestDTO.setBusinessShortCode(businessShortCode);

        buyGoodsPaymentRequestDTO.setAmount(amount);
        buyGoodsPaymentRequestDTO.setPassword(getPassword(
                ""+businessShortCode, passKey, "" + timestamp
        ));
        buyGoodsPaymentRequestDTO.setTransactionType(MPESA_BUY_GOODS_TRANSACTION_TYPE);
        buyGoodsPaymentRequestDTO.setTransactionDesc("Payment from account id" +
                transactionChannelRequestDTO.getPayer()[1].getValue());
        buyGoodsPaymentRequestDTO.setAccountReference("Payment to " + businessShortCode);


        return buyGoodsPaymentRequestDTO;
    }

    public Long getTimestamp() {
        return Long.parseLong(sdf.format(new Date()));
    }

    /*
     * Return the result code from the callback received from mpesa server
     */
    public static StkCallback getStkCallback(JsonObject callback) {
        StkCallback stkCallback = new StkCallback();

        LinkedHashMap<String, Object> body = (LinkedHashMap<String, Object>) callback.get("Body");
        LinkedHashMap<String, Object> stkCallbackFromServer = (LinkedHashMap<String, Object>) body.get("stkCallback");
        stkCallback.setMerchantRequestId((String) stkCallbackFromServer.get("MerchantRequestID"));
        stkCallback.setCheckoutRequestId((String) stkCallbackFromServer.get("CheckoutRequestID"));
        stkCallback.setResultCode(Long.parseLong(String.valueOf(stkCallbackFromServer.get("ResultCode"))));
        stkCallback.setResultDesc((String) stkCallbackFromServer.get("ResultDesc"));

        return stkCallback;
    }

    /*
     * Return the transaction id from the callback received from mpesa server
     */
    public static String getTransactionId(JsonObject callback) {
        AtomicReference<String> mpesaReceiptNumber = new AtomicReference<>("");
        LinkedHashMap<String, Object> body = (LinkedHashMap<String, Object>) callback.get("Body");
        LinkedHashMap<String, Object> metaData = (LinkedHashMap<String, Object>) body.get("CallbackMetadata");
        ArrayList<Object> metaDataItems = (ArrayList) metaData.get("Item");

        metaDataItems.forEach(metaDataItem -> {
            LinkedHashMap<String, Object> item = (LinkedHashMap<String, Object>) metaDataItem;
            if(item.get("Name").equals("MpesaReceiptNumber")) {
                mpesaReceiptNumber.set((String) item.get("Value"));
            }
        });
        return String.valueOf(mpesaReceiptNumber);
    }

    /*
     * Generated the password using the businessShortCode, passKey and timestamp
     * @param businessShortCode
     * @param passKey
     * @param timestamp
     * @return password
     */
    public String getPassword(String businessShortCode, String passKey, String timestamp) {
        businessShortCode = businessShortCode.replace("\n", "");
        passKey = passKey.replace("\n", "");
        timestamp = timestamp.replace("\n", "");
        String data = businessShortCode + passKey + timestamp;
        return toBase64(data);
    }

    /*
     * Converts the string data into base64 encode string
     * @param data
     * @return base64 of [data]
     */
    public String toBase64(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }


}
