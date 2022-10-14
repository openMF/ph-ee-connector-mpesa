package org.mifos.connector.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * {
 * "Initiator": "",
 * "SecurityCredential": "",
 * "CommandID": "TransactionStatusQuery",
 * "TransactionID": "",
 * "PartyA": "",
 * "IdentifierType": "1",
 * "ResultURL": "",
 * "QueueTimeOutURL": "",
 * "Remarks": "",
 * "Occasion": ""
 * }
 */
@Getter
@Setter
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
}
