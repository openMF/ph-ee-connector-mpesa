package org.mifos.connector.mpesa.utility;

public class ShortCodeAms {
    String businessShortCode;
    String ams;
    String currency;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBusinessShortCode() {
        return this.businessShortCode;
    }

    public String getAms() {
        return this.ams;
    }

    public void setBusinessShortCode(String businessShortCode) {
        this.businessShortCode = businessShortCode;
    }

    public void setAms(String ams) {
        this.ams = ams;
    }
}
