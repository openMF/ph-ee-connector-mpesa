package org.mifos.connector.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {
 *     "Initiator": "",
 *     "SecurityCredential": "",
 *     "CommandID": "TransactionStatusQuery",
 *     "TransactionID": "",
 *     "PartyA": "",
 *     "IdentifierType": "1",
 *     "ResultURL": "",
 *     "QueueTimeOutURL": "",
 *     "Remarks": "",
 *     "Occasion": ""
 * }
 */
public class TransactionStatusRequestDTO {

    @JsonProperty("BusinessShortCode")
    private Long businessShortCode;

    @JsonProperty("Password")
    private String password;

    @JsonProperty("Timestamp")
    private String timestamp;


    @JsonProperty("CheckoutRequestID")
    private String checkoutRequestId;

    public TransactionStatusRequestDTO() {
    }

    @Override
    public String toString() {
        return "TransactionStatusRequestDTO{" +
                "businessShortCode=" + businessShortCode +
                ", password='" + password + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", checkoutRequestId='" + checkoutRequestId + '\'' +
                '}';
    }

    public Long getBusinessShortCode() {
        return businessShortCode;
    }

    public void setBusinessShortCode(Long businessShortCode) {
        this.businessShortCode = businessShortCode;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCheckoutRequestId() {
        return checkoutRequestId;
    }

    public void setCheckoutRequestId(String checkoutRequestId) {
        this.checkoutRequestId = checkoutRequestId;
    }
}
