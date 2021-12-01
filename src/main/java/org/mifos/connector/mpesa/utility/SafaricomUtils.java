package org.mifos.connector.mpesa.utility;

import org.apache.camel.util.json.JsonArray;
import org.apache.camel.util.json.JsonObject;
import org.mifos.connector.common.channel.dto.TransactionChannelCollectionRequestDTO;
import org.mifos.connector.common.channel.dto.TransactionChannelRequestDTO;
import org.mifos.connector.mpesa.dto.BuyGoodsPaymentRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.mifos.connector.mpesa.safaricom.config.SafaricomProperties.MPESA_BUY_GOODS_TRANSACTION_TYPE;

@Component
public class SafaricomUtils {

    @Value("${mpesa.local.host}")
    private String localhost;

    @Value("${mpesa.local.transaction-callback}")
    private String callbackEndpoint;

    @Value("${mpesa.api.pass-key}")
    private String passKey;

    @Value("${mpesa.business-short-code}")
    private Long businessShortCode;

    public BuyGoodsPaymentRequestDTO channelRequestConvertor(TransactionChannelCollectionRequestDTO transactionChannelRequestDTO) {
        BuyGoodsPaymentRequestDTO buyGoodsPaymentRequestDTO = new BuyGoodsPaymentRequestDTO();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");


        // parsing amount from USD 123
        long amount = Long.parseLong(transactionChannelRequestDTO.getAmount().split(" ")[1]);
        long timestamp = 123; //Long.parseLong(sdf.format(new Date()));
        long payer = Long.parseLong(transactionChannelRequestDTO.getPayer()[0].getValue());

        buyGoodsPaymentRequestDTO.setTimestamp(""+timestamp);
        buyGoodsPaymentRequestDTO.setCallBackURL("https://3ea9-103-85-119-3.ngrok.io" + callbackEndpoint);

        buyGoodsPaymentRequestDTO.setPartyA(payer);
        buyGoodsPaymentRequestDTO.setPhoneNumber(payer);

        buyGoodsPaymentRequestDTO.setPartyB(businessShortCode);
        buyGoodsPaymentRequestDTO.setBusinessShortCode(businessShortCode);

        buyGoodsPaymentRequestDTO.setAmount(amount);
        buyGoodsPaymentRequestDTO.setPassword(SafaricomUtils.getPassword(
                ""+businessShortCode, passKey, "" + timestamp
        ));
        buyGoodsPaymentRequestDTO.setTransactionType(MPESA_BUY_GOODS_TRANSACTION_TYPE);
        buyGoodsPaymentRequestDTO.setTransactionDesc("Payment from account id" +
                transactionChannelRequestDTO.getPayer()[1].getValue());
        buyGoodsPaymentRequestDTO.setAccountReference("Payment to " + businessShortCode);


        return buyGoodsPaymentRequestDTO;
    }

    /*
     * Return the transaction id from the callback received from mpesa server
     */
    public static String getTransactionId(JsonObject callback) {
        AtomicReference<String> mpesaReceiptNumber = new AtomicReference<>("");
        JsonObject body = (JsonObject) callback.get("Body");
        JsonObject metaData = (JsonObject) body.get("CallbackMetadata");
        JsonArray metaDataItems = (JsonArray) metaData.get("items");

        metaDataItems.forEach(metaDataItem -> {
            JsonObject item = (JsonObject) metaDataItem;
            if(item.getString("Name").equals("MpesaReceiptNumber")) {
                mpesaReceiptNumber.set(item.getString("value"));
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
    public static String getPassword(String businessShortCode, String passKey, String timestamp) {
        String data = businessShortCode + passKey + timestamp;
        String password = toBase64(data);
        return password;
    }

    /*
     * Converts the string data into base64 encode string
     * @param data
     * @return base64 of [data]
     */
    public static String toBase64(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

}
