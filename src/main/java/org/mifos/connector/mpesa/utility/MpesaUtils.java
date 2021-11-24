package org.mifos.connector.mpesa.utility;

import org.apache.camel.util.json.JsonArray;
import org.apache.camel.util.json.JsonObject;

import java.util.concurrent.atomic.AtomicReference;

public class MpesaUtils {

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

}
