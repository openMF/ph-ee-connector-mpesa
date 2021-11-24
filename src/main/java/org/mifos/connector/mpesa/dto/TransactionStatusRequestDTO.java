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

    @JsonProperty("Initiator")
    private String initiator;

    @JsonProperty("SecurityCredential")
    private String securityCredential;

    @JsonProperty("CommandID")
    private String commandId;

    @JsonProperty("TransactionID")
    private String transactionId;

    @JsonProperty("PartyA")
    private String partyA;

    @JsonProperty("IdentifierType")
    private String identifierType;

    @JsonProperty("ResultURL")
    private String resultUrl;

    @JsonProperty("QueueTimeOutURL")
    private String queueTimeOutUrl;

    @JsonProperty("Remarks")
    private String remarks;

    @JsonProperty("Occasion")
    private String occasion;

    public TransactionStatusRequestDTO() {
    }

    @Override
    public String toString() {
        return "TransactionStatusRequestDTO{" +
                "initiator='" + initiator + '\'' +
                ", securityCredential='" + securityCredential + '\'' +
                ", commandId='" + commandId + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", partyA='" + partyA + '\'' +
                ", identifierType='" + identifierType + '\'' +
                ", resultUrl='" + resultUrl + '\'' +
                ", queueTimeOutUrl='" + queueTimeOutUrl + '\'' +
                ", remarks='" + remarks + '\'' +
                ", occasion='" + occasion + '\'' +
                '}';
    }

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public String getSecurityCredential() {
        return securityCredential;
    }

    public void setSecurityCredential(String securityCredential) {
        this.securityCredential = securityCredential;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getPartyA() {
        return partyA;
    }

    public void setPartyA(String partyA) {
        this.partyA = partyA;
    }

    public String getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(String identifierType) {
        this.identifierType = identifierType;
    }

    public String getResultUrl() {
        return resultUrl;
    }

    public void setResultUrl(String resultUrl) {
        this.resultUrl = resultUrl;
    }

    public String getQueueTimeOutUrl() {
        return queueTimeOutUrl;
    }

    public void setQueueTimeOutUrl(String queueTimeOutUrl) {
        this.queueTimeOutUrl = queueTimeOutUrl;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getOccasion() {
        return occasion;
    }

    public void setOccasion(String occasion) {
        this.occasion = occasion;
    }
}
