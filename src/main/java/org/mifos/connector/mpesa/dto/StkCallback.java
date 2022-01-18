package org.mifos.connector.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {
 *          "MerchantRequestID":"22198-13975659-1",
 *          "CheckoutRequestID":"ws_CO_180120222013292210",
 *          "ResultCode":1,
 *          "ResultDesc":"The balance is insufficient for the transaction"
 * }
 */
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

    public String getMerchantRequestId() {
        return merchantRequestId;
    }

    public void setMerchantRequestId(String merchantRequestId) {
        this.merchantRequestId = merchantRequestId;
    }

    public String getCheckoutRequestId() {
        return checkoutRequestId;
    }

    public void setCheckoutRequestId(String checkoutRequestId) {
        this.checkoutRequestId = checkoutRequestId;
    }

    public Long getResultCode() {
        return resultCode;
    }

    public void setResultCode(Long resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultDesc() {
        return resultDesc;
    }

    public void setResultDesc(String resultDesc) {
        this.resultDesc = resultDesc;
    }
}
