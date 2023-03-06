package org.mifos.connector.mpesa.utility;

import org.json.JSONObject;
import org.mifos.connector.common.gsma.dto.CustomData;
import org.mifos.connector.common.gsma.dto.GsmaTransfer;
import org.mifos.connector.common.gsma.dto.Party;
import org.mifos.connector.mpesa.dto.ChannelRequestDTO;
import org.mifos.connector.mpesa.dto.ChannelSettlementRequestDTO;
import org.mifos.connector.mpesa.dto.PaybillRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


@Component
public class MpesaUtils {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private MpesaAMSProp mpesaAMSProp;

    private List<MpesaProps.MPESA> mpesa;

    private String process = "process";

    @Value("${paygops.host}")
    private String paygopsHost;
    @Value("${roster.host}")
    private String rosterHost;

    enum ams {
        paygops,
        roster;
    }

    public GsmaTransfer createGsmaTransferDTO(JSONObject paybillResponseBodyString) {
        GsmaTransfer gsmaTransfer = new GsmaTransfer();

        List<CustomData> customData = setCustomData(paybillResponseBodyString);
        String currentDateTime = getCurrentDateTime();

        Party payer = new Party();
        payer.setPartyIdIdentifier(paybillResponseBodyString.getString("msisdn"));
        payer.setPartyIdType("MSISDN");
        List<Party> payerObj = new ArrayList<>();
        payerObj.add(payer);

        Party payee = new Party();
        payee.setPartyIdIdentifier(paybillResponseBodyString.getString("msisdn"));
        payee.setPartyIdType("accountId");
        List<Party> payeeObj = new ArrayList<>();
        payeeObj.add(payee);

        gsmaTransfer.setCustomData(customData);
        gsmaTransfer.setRequestDate(currentDateTime);
        gsmaTransfer.setPayee(payeeObj);
        gsmaTransfer.setPayer(payeeObj);
        gsmaTransfer.setSubType("inbound");
        gsmaTransfer.setType("transfer");
        gsmaTransfer.setDescriptionText("description");
        gsmaTransfer.setRequestingOrganisationTransactionReference(paybillResponseBodyString.getString("transactionId"));
        gsmaTransfer.setAmount(paybillResponseBodyString.getString("amount"));
        gsmaTransfer.setCurrency(paybillResponseBodyString.getString("currency"));

        return gsmaTransfer;
    }

    private List<CustomData> setCustomData(JSONObject paybillResponseBodyString) {
        CustomData reconciled = new CustomData();
        reconciled.setKey("partyLookupFailed");
        reconciled.setValue(String.valueOf(paybillResponseBodyString.getBoolean("reconciled")));
        CustomData confirmationReceived = new CustomData();
        confirmationReceived.setKey("confirmationReceived");
        confirmationReceived.setValue(String.valueOf(false));
        List<CustomData> customData = new ArrayList<>();
        customData.add(reconciled);
        customData.add(confirmationReceived);
        return customData;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date date = new Date();
        return formatter.format(date);
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

    public String getAMSUrl(String amsName) {

        if (Objects.equals(amsName, ams.paygops.toString())) {
            return paygopsHost;
        } else if (Objects.equals(amsName, ams.roster.toString())) {
            return rosterHost;
        }
        return null;
    }

    public static ChannelRequestDTO convertPaybillPayloadToChannelPayload(PaybillRequestDTO paybillRequestDTO, String amsName, String currency) {
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

        JSONObject currencyObj = new JSONObject();
        currencyObj.put("key", "currency");
        currencyObj.put("value", currency);

        JSONObject memo = new JSONObject();
        memo.put("key", "memo");
        memo.put("value", foundationalId);

        JSONObject walletName = new JSONObject();
        walletName.put("key", "wallet_name");
        walletName.put("value", paybillRequestDTO.getMsisdn());

        JSONObject amount = new JSONObject();
        amount.put("key", "amount");
        amount.put("value", paybillRequestDTO.getTransactionAmount());

        customData.add(transactionId);
        customData.add(currencyObj);
        customData.add(memo);
        customData.add(walletName);
        customData.add(amount);

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