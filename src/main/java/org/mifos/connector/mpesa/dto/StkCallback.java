package org.mifos.connector.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * {
 * "MerchantRequestID":"22198-13975659-1",
 * "CheckoutRequestID":"ws_CO_180120222013292210",
 * "ResultCode":1,
 * "ResultDesc":"The balance is insufficient for the transaction"
 * }
 */
@Getter
@Setter
public class StkCallback {

    @JsonProperty("MerchantRequestID")
    private String merchantRequestId;

    @JsonProperty("CheckoutRequestID")
    private String checkoutRequestId;

    @JsonProperty("ResultCode")
    private Long resultCode;

    @JsonProperty("ResultDesc")
    private String resultDesc;

    public StkCallback() {
    }

    @Override
    public String toString() {
        return "StkCallback{" +
                "merchantRequestId='" + merchantRequestId + '\'' +
                ", checkoutRequestId='" + checkoutRequestId + '\'' +
                ", resultCode=" + resultCode +
                ", resultDesc='" + resultDesc + '\'' +
                '}';
    }
}
