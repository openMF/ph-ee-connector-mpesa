package org.mifos.connector.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


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

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public String getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(String transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(String transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getBillRefNo() {
        return billRefNo;
    }

    public void setBillRefNo(String billRefNo) {
        this.billRefNo = billRefNo;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(String accountBalance) {
        this.accountBalance = accountBalance;
    }

    public String getThirdPatrytransactionID() {
        return thirdPatrytransactionID;
    }

    public void setThirdPatrytransactionID(String thirdPatrytransactionID) {
        this.thirdPatrytransactionID = thirdPatrytransactionID;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
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
