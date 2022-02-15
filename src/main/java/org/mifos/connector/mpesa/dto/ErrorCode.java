package org.mifos.connector.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorCode {

    @JsonProperty("id")
    int id;

    @JsonProperty("transactionType")
    String transactionType;

    @JsonProperty("errorMessage")
    String errorMessage;

    @JsonProperty("errorCode")
    String errorCode;

    @JsonProperty("recoverable")
    boolean recoverable;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isRecoverable() {
        return recoverable;
    }

    public void setRecoverable(boolean recoverable) {
        this.recoverable = recoverable;
    }

}
