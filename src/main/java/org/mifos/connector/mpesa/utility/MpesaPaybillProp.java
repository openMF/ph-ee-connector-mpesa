package org.mifos.connector.mpesa.utility;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "paybill")
public class MpesaPaybillProp {

    private List<MpesaPaybillProp.ShortCode> group;

    public List<MpesaPaybillProp.ShortCode> getGroup() {
        return group;
    }

    public void setGroup(List<MpesaPaybillProp.ShortCode> shortCodeGroup) {
        this.group = shortCodeGroup;
    }

    String currency;
    String accountHoldingInstitutionId;

    public String getCurrency() {
        return this.currency;
    }

    public String getAccountHoldingInstitutionId() {
        return accountHoldingInstitutionId;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setAccountHoldingInstitutionId(String accountHoldingInstitutionId) {
        this.accountHoldingInstitutionId = accountHoldingInstitutionId;
    }

    public class ShortCode {
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
}
