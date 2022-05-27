package org.mifos.connector.mpesa.utility;


public class ConnectionUtils {

    /**
     * returns camel dsl for applying connection timeout
     * @param timeout timeout value in ms
     * @return
     */
    public static String getConnectionTimeoutDsl(int timeout) {
        String base = "httpClient.connectTimeout={}&httpClient.connectionRequestTimeout={}&httpClient.socketTimeout={}";
        return base.replace("{}", ""+timeout);
    }
}
