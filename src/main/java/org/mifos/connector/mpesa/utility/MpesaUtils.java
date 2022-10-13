package org.mifos.connector.mpesa.utility;

import org.json.JSONObject;
import org.mifos.connector.mpesa.dto.ChannelRequestDTO;
import org.mifos.connector.mpesa.dto.PaybillRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Component
public class MpesaUtils {

    @Autowired
    private MpesaAMSProp mpesaAMSProp;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<MpesaProps.MPESA> mpesa;

    private String process = "process";

    @Value("${paygops.host}")
    private static String paygopsHost;
    @Value("${roster.host}")
    private static String rosterHost;

    enum ams {
        paygops,
        roster;
    }

    public static String getAMSUrl(String amsName) {

        if (Objects.equals(amsName, ams.paygops.toString())) {
            return paygopsHost;
        } else if (Objects.equals(amsName, ams.roster.toString())) {
            return rosterHost;
        }
        return null;
    }

    public static ChannelRequestDTO convertPaybillPayloadToChannelPayload(PaybillRequestDTO paybillRequestDTO, String amsName) {
        String foundationalId = "";
        String accountID = "";
        // Mapping primary and secondary Identifier
        JSONObject primaryIdentifier = new JSONObject();
        if (amsName.equalsIgnoreCase("paygops")) {
            foundationalId = paybillRequestDTO.getBillRefNo();
            primaryIdentifier.put("key", "foundationalId");
            primaryIdentifier.put("value", foundationalId);
        } else if (amsName.equalsIgnoreCase("roster")) {
            accountID = paybillRequestDTO.getBillRefNo();
            primaryIdentifier.put("key", "accountID");
            primaryIdentifier.put("value", accountID);
        }
        JSONObject secondaryIdentifier = new JSONObject();
        secondaryIdentifier.put("key", "MSISDN");
        secondaryIdentifier.put("value", paybillRequestDTO.getMsisdn());
        // Mapping custom data
        List<JSONObject> customData = new ArrayList<>();

        JSONObject transactionId = new JSONObject();
        transactionId.put("key", "transactionId");
        transactionId.put("value", paybillRequestDTO.getTransactionID());

        JSONObject currency = new JSONObject();
        currency.put("key", "currency");
        currency.put("value", "KES");

        JSONObject memo = new JSONObject();
        memo.put("key", "memo");
        memo.put("value", foundationalId);

        JSONObject walletName = new JSONObject();
        walletName.put("key", "wallet_name");
        walletName.put("value", paybillRequestDTO.getMsisdn());

        customData.add(transactionId);
        customData.add(currency);
        customData.add(memo);
        customData.add(walletName);
        ChannelRequestDTO channelRequestDTO = new ChannelRequestDTO();
        channelRequestDTO.setPrimaryIdentifier(primaryIdentifier);
        channelRequestDTO.setSecondaryIdentifier(secondaryIdentifier);
        channelRequestDTO.setCustomData(customData);

        return channelRequestDTO;
    }

    public String getProcess() {
        return process;
    }

    public List<MpesaProps.MPESA> getGroup() {
        mpesa = mpesaAMSProp.getGroup();
        return mpesa;
    }

    public MpesaProps.MPESA setMpesaProperties() {
        MpesaProps.MPESA properties = null;
        List<MpesaProps.MPESA> groups = getGroup();
        for (MpesaProps.MPESA identifier : groups) {
            String name = identifier.getName();
            String process = getProcess();
            if (process.contains(name)) {
                properties = identifier;
                break;

            } else {
                if (name.equals("default")) {
                    properties = identifier;
                }
            }
        }
        return properties;
    }


    public void setProcess(String process) {
        logger.info("Process Value being set");
        this.process = process;
    }

    public static String maskString(String strText) {

        char maskChar = '*';
        int start = 0;
        int end = strText.length() - 4;

        if (start > end) {
            return "***";
        }

        int maskLength = end - start;

        if (maskLength == 0)
            return strText;

        StringBuilder sbMaskString = new StringBuilder(maskLength);

        for (int i = 0; i < maskLength; i++) {
            sbMaskString.append(maskChar);
        }

        return strText.substring(0, start) + sbMaskString + strText.substring(start + maskLength);
    }

    public static void main(String[] args) {
        String dt = "254708374149";
        System.out.println(maskString(dt));
    }

}