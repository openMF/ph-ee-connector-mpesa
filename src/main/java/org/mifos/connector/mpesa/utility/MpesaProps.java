package org.mifos.connector.mpesa.utility;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Configuration
@ConfigurationProperties(prefix = "accounts")
public class MpesaProps {

    private List<MPESA> group;

    public List<MPESA> getGroup() {
        return group;
    }

    public void setGroup(List<MPESA> amsgroup) {
        this.group = amsgroup;
    }


    public static class MPESA{

        private String name;
        private String businessShortCode;
        private String till;
        private String authHost;
        private String clientKey;
        private String clientSecret;
        private String apiHost;
        private String passKey;


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getBusinessShortCode() {
            return Long.parseLong(businessShortCode);
        }

        public void setBusinessShortCode(String businessShortCode) {
            this.businessShortCode = businessShortCode.trim();
        }

        public Long getTill() {
            return Long.parseLong(till);
        }

        public void setTill(String till) {
            this.till = till.trim();
        }

        public String getAuthHost() {
            return authHost;
        }

        public void setAuthHost(String authHost) {
            this.authHost = authHost;
        }

        public String getClientKey() {
            return clientKey;
        }

        public void setClientKey(String clientKey) {
            this.clientKey = clientKey;
        }

        public String getApiHost() {
            return apiHost;
        }

        public void setApiHost(String apiHost) {
            this.apiHost = apiHost;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getPassKey() {
            return passKey;
        }

        public void setPassKey(String passKey) {
            this.passKey = passKey;
        }

        public String getDefaultClientKey(){
            String key = null;
            if(getName().equals("default")){
               key =  getClientKey();
            }
            return key;
        }
        public String getDefaultClientSecret(){
            String secret = null;
            if(getName().equals("default")){
                secret =  getClientSecret();
            }
            return secret;
        }
    }
}

