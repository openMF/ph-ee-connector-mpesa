package org.mifos.connector.mpesa.utility;

import org.apache.camel.util.json.JsonArray;
import org.apache.camel.util.json.JsonObject;
import org.mifos.connector.common.channel.dto.TransactionChannelRequestDTO;
import org.mifos.connector.mpesa.dto.BuyGoodsPaymentRequestDTO;
import org.springframework.beans.factory.annotation.Value;

import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import static org.mifos.connector.mpesa.safaricom.config.SafaricomProperties.MPESA_BUY_GOODS_TRANSACTION_TYPE;

public class SafaricomUtils {

    @Value("${mpesa.local.host}")
    private static String localhost;

    @Value("${mpesa.local.host}")
    private static String callbackEndpoint;

    @Value("${mpesa.api}")
    private static String passKey;

    public static BuyGoodsPaymentRequestDTO channelRequestConvertor(TransactionChannelRequestDTO transactionChannelRequestDTO) {
        BuyGoodsPaymentRequestDTO buyGoodsPaymentRequestDTO = new BuyGoodsPaymentRequestDTO();

        String payee = transactionChannelRequestDTO.getPayee().getPartyIdInfo().getPartyIdentifier();
        String payer = transactionChannelRequestDTO.getPayer().getPartyIdInfo().getPartyIdentifier();
        String amount = transactionChannelRequestDTO.getAmount().getAmount();
        Long timestamp = System.currentTimeMillis();

        buyGoodsPaymentRequestDTO.setTimestamp(""+timestamp);
        buyGoodsPaymentRequestDTO.setCallBackURL(localhost+callbackEndpoint);
        buyGoodsPaymentRequestDTO.setPartyA(Long.parseLong(payer));
        buyGoodsPaymentRequestDTO.setPhoneNumber(Long.parseLong(payer));
        buyGoodsPaymentRequestDTO.setPartyB(Long.parseLong(payee));
        buyGoodsPaymentRequestDTO.setBusinessShortCode(Long.parseLong(payee));
        buyGoodsPaymentRequestDTO.setAmount(Long.parseLong(amount));
        buyGoodsPaymentRequestDTO.setPassword(SafaricomUtils.getPassword(
                payer, passKey, "" + timestamp
        ));
        buyGoodsPaymentRequestDTO.setTransactionType(MPESA_BUY_GOODS_TRANSACTION_TYPE);
        buyGoodsPaymentRequestDTO.setTransactionDesc("Payment from " + transactionChannelRequestDTO.getPayee().getName() +
                "\n" + "Payment to " + transactionChannelRequestDTO.getPayer().getName()
        );
        buyGoodsPaymentRequestDTO.setAccountReference(transactionChannelRequestDTO.getPayee().getName());


        return new BuyGoodsPaymentRequestDTO();
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
