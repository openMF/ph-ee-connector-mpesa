package org.mifos.connector.mpesa.utility;

public class ShortCodeAms {
    String businessShortCode;
    String ams;

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

    public String getDefaultAms() {
        String ams = null;
        if (getBusinessShortCode().equals("default")) {
            ams = getAms();
        }
        return ams;
    }
}
