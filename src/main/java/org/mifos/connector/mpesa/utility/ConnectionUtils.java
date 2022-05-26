package org.mifos.connector.mpesa.utility;


public class ConnectionUtils {

    /**
     * returns camel dsl for applying connection timeout
     * @param timeout timeout value in ms
     * @return
     */
    public static String getConnectionTimeoutDsl(int timeout) {
        String base = "httpClient.connectTimeout=%s&httpClient.connectionRequestTimeout=%s&httpClient.socketTimeout=%s";
        return String.format(base, timeout);
    }
}
