package org.mifos.connector.mpesa.utility;

import com.google.api.client.util.DateTime;
import org.json.JSONObject;
import org.mifos.connector.mpesa.dto.ChannelRequestDTO;
import org.mifos.connector.mpesa.dto.ChannelSettlementRequestDTO;
import org.mifos.connector.mpesa.dto.PaybillRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Component
public class MpesaUtils {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MpesaAMSProp mpesaAMSProp;

    @Autowired
    private MpesaPaybillProp mpesaPaybillProp;

    List<MpesaPaybillProp.ShortCode> shortCodeList;
    private List<MpesaProps.MPESA> mpesa;

    private String process = "process";

    @Value("${paygops.host}")
    private static String paygopsHost;
    @Value("${roster.host}")
    private static String rosterHost;

    @PostConstruct
    public List<MpesaPaybillProp.ShortCode> postConstruct() {
        shortCodeList = mpesaPaybillProp.getShortCodeList();
        return shortCodeList;
    }

    public static ChannelSettlementRequestDTO convertPaybillToChannelPayload(PaybillRequestDTO paybillConfirmationRequestDTO, String amsName, String currency) {
        JSONObject payer = new JSONObject();

        JSONObject partyIdInfoPayer = new JSONObject();
        partyIdInfoPayer.put("partyIdType", "MSISDN");
        partyIdInfoPayer.put("partyIdentifier", paybillConfirmationRequestDTO.getMsisdn());

        payer.put("partyIdInfo", partyIdInfoPayer);

        JSONObject payee = new JSONObject();
        JSONObject partyIdInfoPayee = new JSONObject();
        if (amsName.equalsIgnoreCase("paygops")) {
            partyIdInfoPayee.put("partyIdType", "FOUNDATIONALID");
            partyIdInfoPayee.put("partyIdentifier", paybillConfirmationRequestDTO.getBillRefNo());
        } else if (amsName.equalsIgnoreCase("roster")) {
            partyIdInfoPayee.put("partyIdType", "ACCOUNTID");
            partyIdInfoPayee.put("partyIdentifier", paybillConfirmationRequestDTO.getBillRefNo());
        }
        payee.put("partyIdInfo", partyIdInfoPayee);

        JSONObject amount = new JSONObject();
        amount.put("amount", paybillConfirmationRequestDTO.getTransactionAmount());
        amount.put("currency", currency);

        return new ChannelSettlementRequestDTO(payer, payee, amount);
    }

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

    public static ChannelRequestDTO convertPaybillPayloadToChannelPayload(PaybillRequestDTO paybillRequestDTO, String amsName, String currency) {
        String requestingOrganisationTransactionReference = paybillRequestDTO.getTransactionID();
        String subtype = "inbound";
        String type = "transfer";
        String amount = paybillRequestDTO.getTransactionAmount();
        String descriptionText = "Paybill inbound transfer";
        String requestDate = new DateTime("yyyyMMdd_HHmmss").toString();

        // Mapping primary and secondary Identifier
        List<JSONObject> payee = new ArrayList<>();
        JSONObject payeeObj = new JSONObject();
        if (amsName.equalsIgnoreCase("paygops")) {
            payeeObj.put("partyIdType", "foundationalId");
            payeeObj.put("partyIdIdentifier", paybillRequestDTO.getBillRefNo());
            payee.add(payeeObj);
        } else if (amsName.equalsIgnoreCase("roster")) {
            payeeObj.put("partyIdType", "accountId");
            payeeObj.put("partyIdIdentifier", paybillRequestDTO.getBillRefNo());
            payee.add(payeeObj);
        }
        List<JSONObject> payer = new ArrayList<>();
        JSONObject payerObj = new JSONObject();
        payerObj.put("partyIdType", "MSISDN");
        payerObj.put("partyIdIdentifier", paybillRequestDTO.getMsisdn());
        payer.add(payerObj);
        // Mapping custom data
        List<JSONObject> customData = new ArrayList<>();

        JSONObject transactionId = new JSONObject();
        transactionId.put("key", "transactionId");
        transactionId.put("value", paybillRequestDTO.getTransactionID());

        JSONObject currencyObj = new JSONObject();
        currencyObj.put("key", "currency");
        currencyObj.put("value", currency);

        JSONObject memo = new JSONObject();
        memo.put("key", "memo");
        memo.put("value", paybillRequestDTO.getBillRefNo());

        JSONObject walletName = new JSONObject();
        walletName.put("key", "wallet_name");
        walletName.put("value", paybillRequestDTO.getMsisdn());

        customData.add(transactionId);
        customData.add(currencyObj);
        customData.add(memo);
        customData.add(walletName);

        ChannelRequestDTO channelRequestDTO = new ChannelRequestDTO();
        channelRequestDTO.setRequestDate(requestDate);
        channelRequestDTO.setRequestingOrganisationTransactionReference(requestingOrganisationTransactionReference);
        channelRequestDTO.setType(type);
        channelRequestDTO.setSubType(subtype);
        channelRequestDTO.setAmount(amount);
        channelRequestDTO.setDescriptionText(descriptionText);
        channelRequestDTO.setPayee(payee);
        channelRequestDTO.setPayer(payer);
        channelRequestDTO.setCustomData(customData);

        System.out.println(String.valueOf(channelRequestDTO));
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