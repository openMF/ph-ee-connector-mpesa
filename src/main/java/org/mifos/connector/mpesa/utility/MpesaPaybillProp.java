package org.mifos.connector.mpesa.utility;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "paybill")
public class MpesaPaybillProp {
    private String accountHoldingInstitutionId;

    public String getAccountHoldingInstitutionId() {
        return accountHoldingInstitutionId;
    }

    public void setAccountHoldingInstitutionId(String accountHoldingInstitutionId) {
        this.accountHoldingInstitutionId = accountHoldingInstitutionId;
    }

    private List<ShortCodeAms> groups = new ArrayList<>();

    public List<ShortCodeAms> getGroups() {
        return groups;
    }

    public void setGroup(List<ShortCodeAms> shortCodeGroup) {
        this.groups = shortCodeGroup;
    }

    public String getAMSFromShortCode(String businessShortCode) {
        String amsName = getGroups().stream()
                .filter(p -> p.getBusinessShortCode().equalsIgnoreCase(businessShortCode))
                .findFirst().get().getAms();
        return amsName;
    }

    public String getCurrencyFromShortCode(String businessShortCode) {
        String currency = getGroups().stream()
                .filter(p -> p.getBusinessShortCode().equalsIgnoreCase(businessShortCode))
                .findFirst().get().getCurrency();
        return currency;
    }
}
