package org.mifos.connector.mpesa.utility;

import org.json.JSONObject;

import java.util.Date;

public class ZeebeUtils {

    public static JSONObject getTransferResponseCreateJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("completedTimestamp", ""+System.currentTimeMillis());
        return jsonObject;
    }

}
