package org.mifos.connector.mpesa.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.mifos.connector.mpesa.utility.MpesaUtils;

/**
 * {
 *     "BusinessShortCode": 174379,
 *     "Password": "MTc0Mzc5YmZiMjc5ZjlhYTliZGJjZjE1OGU5N2RkNzFhNDY3Y2QyZTBjODkzMDU5YjEwZjc4ZTZiNzJhZGExZWQyYzkxOTIwMjExMTA1MjI0MTA3",
 *     "Timestamp": "20211105224107",
 *     "TransactionType": "CustomerBuyGoodsOnline",
 *     "Amount": 1,
 *     "PartyA": 254708374149,
 *     "PartyB": 174379,
 *     "PhoneNumber": 254708374149,
 *     "CallBackURL": "https://mydomain.com/path",
 *     "AccountReference": "CompanyXLTD",
 *     "TransactionDesc": "Payment of X"
 * }
 */
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

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getPartyA() {
        return partyA;
    }

    public void setPartyA(Long partyA) {
        this.partyA = partyA;
    }

    public Long getPartyB() {
        return partyB;
    }

    public void setPartyB(Long partyB) {
        this.partyB = partyB;
    }

    public Long getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(Long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCallBackURL() {
        return callBackURL;
    }

    public void setCallBackURL(String callBackURL) {
        this.callBackURL = callBackURL;
    }

    public String getAccountReference() {
        return accountReference;
    }

    public void setAccountReference(String accountReference) {
        this.accountReference = accountReference;
    }

    public String getTransactionDesc() {
        return transactionDesc;
    }

    public void setTransactionDesc(String transactionDesc) {
        this.transactionDesc = transactionDesc;
    }
}
