package org.mifos.connector.mpesa.dto;

import org.json.JSONObject;

public class ChannelSettlementRequestDTO {
    JSONObject payer;
    JSONObject payee;
    JSONObject amount;

    public ChannelSettlementRequestDTO(JSONObject payer, JSONObject payee, JSONObject amount) {
        this.payer = payer;
        this.payee = payee;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "{" +
                "payer:" + payer +
                ", payee:" + payee +
                ", amount:" + amount +
                "}";
    }

    public ChannelSettlementRequestDTO() {
    }

    public JSONObject getPayer() {
        return payer;
    }

    public void setPayer(JSONObject payer) {
        this.payer = payer;
    }

    public JSONObject getPayee() {
        return payee;
    }

    public void setPayee(JSONObject payee) {
        this.payee = payee;
    }

    public JSONObject getAmount() {
        return amount;
    }

    public void setAmount(JSONObject amount) {
        this.amount = amount;
    }
}
