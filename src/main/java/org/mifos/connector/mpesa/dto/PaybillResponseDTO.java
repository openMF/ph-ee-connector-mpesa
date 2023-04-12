package org.mifos.connector.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaybillResponseDTO {
    @JsonProperty("reconciled")
    private boolean reconciled;

    @JsonProperty("amsName")
    private String amsName;

    @JsonProperty("accountHoldingInstitutionId")
    private String accountHoldingInstitutionId;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("msisdn")
    private String msisdn;

    public PaybillResponseDTO() {
    }

    public PaybillResponseDTO(boolean reconciled, String amsName, String accountHoldingInstitutionId, String transactionId, String amount, String currency, String msisdn) {
        this.reconciled = reconciled;
        this.amsName = amsName;
        this.accountHoldingInstitutionId = accountHoldingInstitutionId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.msisdn = msisdn;
    }

    @Override
    public String toString() {
        return "PaybillResponseDTO{" +
                "reconciled=" + reconciled +
                ", amsName='" + amsName + '\'' +
                ", accountHoldingInstitutionId='" + accountHoldingInstitutionId + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", amount='" + amount + '\'' +
                ", currency='" + currency + '\'' +
                ", msisdn='" + msisdn + '\'' +
                '}';
    }

    public boolean isReconciled() {
        return reconciled;
    }

    public void setReconciled(boolean reconciled) {
        this.reconciled = reconciled;
    }

    public String getAmsName() {
        return amsName;
    }

    public void setAmsName(String amsName) {
        this.amsName = amsName;
    }

    public String getAccountHoldingInstitutionId() {
        return accountHoldingInstitutionId;
    }

    public void setAccountHoldingInstitutionId(String accountHoldingInstitutionId) {
        this.accountHoldingInstitutionId = accountHoldingInstitutionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }
}
