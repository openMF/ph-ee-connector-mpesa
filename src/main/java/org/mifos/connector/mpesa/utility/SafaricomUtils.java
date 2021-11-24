package org.mifos.connector.mpesa.utility;

import java.util.Base64;

public class SafaricomUtils {

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
