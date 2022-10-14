package org.mifos.connector.mpesa.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.mifos.connector.mpesa.utility.MpesaUtils;

/**
 * {
 * "BusinessShortCode": 174379,
 * "Password": "MTc0Mzc5YmZiMjc5ZjlhYTliZGJjZjE1OGU5N2RkNzFhNDY3Y2QyZTBjODkzMDU5YjEwZjc4ZTZiNzJhZGExZWQyYzkxOTIwMjExMTA1MjI0MTA3",
 * "Timestamp": "20211105224107",
 * "TransactionType": "CustomerBuyGoodsOnline",
 * "Amount": 1,
 * "PartyA": 254708374149,
 * "PartyB": 174379,
 * "PhoneNumber": 254708374149,
 * "CallBackURL": "https://mydomain.com/path",
 * "AccountReference": "CompanyXLTD",
 * "TransactionDesc": "Payment of X"
 * }
 */
@Getter
@Setter
public class BuyGoodsPaymentRequestDTO {

    @JsonProperty("BusinessShortCode")
    private Long businessShortCode;

    @JsonProperty("Password")
    private String password;

    @JsonProperty("Timestamp")
    private String timestamp;

    @JsonProperty("TransactionType")
    private String transactionType;

    @JsonProperty("Amount")
    private Long amount;

    @JsonProperty("PartyA")
    private Long partyA;

    @JsonProperty("PartyB")
    private Long partyB;

    @JsonProperty("PhoneNumber")
    private Long phoneNumber;

    @JsonProperty("CallBackURL")
    private String callBackURL;

    @JsonProperty("AccountReference")
    private String accountReference;

    @JsonProperty("TransactionDesc")
    private String transactionDesc;

    public BuyGoodsPaymentRequestDTO() {
    }

    @Override
    public String toString() {
        return "BuyGoodsPaymentRequestDTO{" +
                "businessShortCode='" + businessShortCode + '\'' +
                ", password='" + MpesaUtils.maskString(password) + '\'' +
                ", timestamp=" + timestamp +
                ", transactionType='" + transactionType + '\'' +
                ", amount=" + amount +
                ", partyA=" + partyA +
                ", partyB=" + partyB +
                ", phoneNumber=" + phoneNumber +
                ", callBackURL='" + callBackURL + '\'' +
                ", accountReference='" + accountReference + '\'' +
                ", transactionDesc='" + transactionDesc + '\'' +
                '}';
    }
}