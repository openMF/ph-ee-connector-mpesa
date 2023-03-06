package org.mifos.connector.mpesa.utility;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "paybill")
public class MpesaPaybillProp {

    private List<ShortCodeAms> groups = new ArrayList<>();

    public List<ShortCodeAms> getGroups() {
        return groups;
    }

    public void setGroup(List<ShortCodeAms> shortCodeGroup) {
        this.groups = shortCodeGroup;
    }

    public String getAMSFromShortCode(String bussinessShortCode) {
        System.out.println("bussinessShortCode : " + bussinessShortCode);
        String amsName = getGroups().stream()
                .filter(p -> p.getBusinessShortCode().equalsIgnoreCase(bussinessShortCode))
                .findFirst().get().getAms();
        System.out.println("Group: " + amsName);
        return amsName;
    }
}
