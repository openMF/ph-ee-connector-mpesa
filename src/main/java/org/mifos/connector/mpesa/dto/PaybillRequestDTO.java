package org.mifos.connector.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


//    {
//        "TransactionType":"Pay Bill",
//        "TransID":"RKTQDM7W6S",
//        "TransTime":"20191122063845",
//        "TransAmount":"10",
//        "BusinessShortCode":"600638",
//        "BillRefNumber":"A123",
//        "InvoiceNumber":"",
//        "OrgAccountBalance":"49197.00",
//        "ThirdPartyTransID":"",
//        "MSISDN":"2547*****149",
//        "FirstName":"John",
//    }
@Getter
@Setter
public class PaybillRequestDTO {
    @JsonProperty("TransactionType")
    private String transactionType;

    @JsonProperty("TransID")
    private String transactionID;

    @JsonProperty("TransTime")
    private String transactionTime;

    @JsonProperty("TransAmount")
    private String transactionAmount;

    @JsonProperty("BusinessShortCode")
    private String shortCode;

    @JsonProperty("BillRefNumber")
    private String billRefNo;
    @JsonProperty("InvoiceNumber")
    private String invoiceNumber;

    @JsonProperty("OrgAccountBalance")
    private String accountBalance;

    @JsonProperty("ThirdPartyTransID")
    private String thirdPatrytransactionID;

    @JsonProperty("MSISDN")
    private String msisdn;

    @JsonProperty("FirstName")
    private String firstname;

    public PaybillRequestDTO() {
    }

    public PaybillRequestDTO(String transactionType, String transactionID, String transactionTime, String transactionAmount, String shortCode, String billRefNo, String invoiceNumber, String accountBalance, String thirdPatrytransactionID, String msisdn, String firstname) {
        this.transactionType = transactionType;
        this.transactionID = transactionID;
        this.transactionTime = transactionTime;
        this.transactionAmount = transactionAmount;
        this.shortCode = shortCode;
        this.billRefNo = billRefNo;
        this.invoiceNumber = invoiceNumber;
        this.accountBalance = accountBalance;
        this.thirdPatrytransactionID = thirdPatrytransactionID;
        this.msisdn = msisdn;
        this.firstname = firstname;
    }

    @Override
    public String toString() {
        return "PaybillRequestDTO{" +
                "transactionType='" + transactionType + '\'' +
                ", transactionID='" + transactionID + '\'' +
                ", transactionTime='" + transactionTime + '\'' +
                ", transactionAmount='" + transactionAmount + '\'' +
                ", shortCode='" + shortCode + '\'' +
                ", billRefNo='" + billRefNo + '\'' +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", accountBalance='" + accountBalance + '\'' +
                ", thirdPatrytransactionID='" + thirdPatrytransactionID + '\'' +
                ", msisdn='" + msisdn + '\'' +
                ", firstname='" + firstname + '\'' +
                '}';
    }
}
