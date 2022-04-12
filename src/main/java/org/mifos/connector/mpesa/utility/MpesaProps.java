package org.mifos.connector.mpesa.utility;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Configuration
@ConfigurationProperties(prefix = "mpesa")
public class MpesaProps {

    private List<MPESA> amsgroup;

    public List<MPESA> getAmsgroup() {
        return amsgroup;
    }

    public void setAmsgroup(List<MPESA> amsgroup) {
        this.amsgroup = amsgroup;
    }


    public static class MPESA{

        private String name;
        private Long businessShortCode;
        private Long till;
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
            return businessShortCode;
        }

        public void setBusinessShortCode(Long businessShortCode) {
            this.businessShortCode = businessShortCode;
        }

        public Long getTill() {
            return till;
        }

        public void setTill(Long till) {
            this.till = till;
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

