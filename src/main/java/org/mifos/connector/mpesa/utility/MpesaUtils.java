package org.mifos.connector.mpesa.utility;

import org.json.JSONObject;
import org.mifos.connector.common.gsma.dto.CustomData;
import org.mifos.connector.common.gsma.dto.GsmaTransfer;
import org.mifos.connector.common.gsma.dto.Party;
import org.mifos.connector.mpesa.dto.ChannelRequestDTO;
import org.mifos.connector.mpesa.dto.ChannelSettlementRequestDTO;
import org.mifos.connector.mpesa.dto.PaybillRequestDTO;
import org.mifos.connector.mpesa.dto.PaybillResponseDTO;
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

import static org.mifos.connector.mpesa.camel.config.CamelProperties.AMOUNT;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.CURRENCY;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.MEMO;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.TRANSACTION_ID;
import static org.mifos.connector.mpesa.camel.config.CamelProperties.WALLET_NAME;


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

    public GsmaTransfer createGsmaTransferDTO(PaybillResponseDTO paybillResponseDTO, String clientCorrelationId) {
        GsmaTransfer gsmaTransfer = new GsmaTransfer();

        List<CustomData> customData = setCustomData(paybillResponseDTO,clientCorrelationId);
        String currentDateTime = getCurrentDateTime();

        Party payer = new Party();
        payer.setPartyIdIdentifier(paybillResponseDTO.getMsisdn());
        payer.setPartyIdType("MSISDN");
        List<Party> payerObj = new ArrayList<>();
        payerObj.add(payer);

        Party payee = new Party();
        payee.setPartyIdIdentifier(paybillResponseDTO.getMsisdn());
        payee.setPartyIdType("accountId");
        List<Party> payeeObj = new ArrayList<>();
        payeeObj.add(payee);

        gsmaTransfer.setCustomData(customData);
        gsmaTransfer.setRequestDate(currentDateTime);
        gsmaTransfer.setPayee(payeeObj);
        gsmaTransfer.setPayer(payerObj);
        gsmaTransfer.setSubType("inbound");
        gsmaTransfer.setType("transfer");
        gsmaTransfer.setDescriptionText("description");
        gsmaTransfer.setRequestingOrganisationTransactionReference(paybillResponseDTO.getTransactionId());
        gsmaTransfer.setAmount(paybillResponseDTO.getAmount());
        gsmaTransfer.setCurrency(paybillResponseDTO.getCurrency());

        return gsmaTransfer;
    }

    private List<CustomData> setCustomData(PaybillResponseDTO paybillResponseDTO, String clientCorrelationId) {
        CustomData reconciled = new CustomData();
        reconciled.setKey("partyLookupFailed");
        reconciled.setValue(!paybillResponseDTO.isReconciled());
        CustomData confirmationReceived = new CustomData();
        confirmationReceived.setKey("confirmationReceived");
        confirmationReceived.setValue(false);
        CustomData mpesaTxnId = new CustomData();
        mpesaTxnId.setKey("mpesaTxnId");
        mpesaTxnId.setValue(paybillResponseDTO.getTransactionId());
        CustomData ams = new CustomData();
        ams.setKey("ams");
        ams.setValue(paybillResponseDTO.getAmsName());
        CustomData tenantId=new CustomData();
        tenantId.setKey("tenantId");
        tenantId.setValue(paybillResponseDTO.getAccountHoldingInstitutionId());
        CustomData clientCorrelation=new CustomData();
        clientCorrelation.setKey("clientCorrelationId");
        clientCorrelation.setValue(clientCorrelationId);
        CustomData currency = new CustomData();
        currency.setKey("currency");
        currency.setValue(paybillResponseDTO.getCurrency());

        List<CustomData> customData = new ArrayList<>();
        customData.add(reconciled);
        customData.add(confirmationReceived);
        customData.add(mpesaTxnId);
        customData.add(ams);
        customData.add(tenantId);
        customData.add(clientCorrelation);
        customData.add(currency);
        return customData;
    }

    public static String getCurrentDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        Date date = new Date();
        return formatter.format(date);
    }

    public ChannelSettlementRequestDTO convertPaybillToChannelPayload(PaybillRequestDTO paybillConfirmationRequestDTO, String amsName, String currency) {
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
        ChannelSettlementRequestDTO channelSettlementRequestDTO = new ChannelSettlementRequestDTO(payer, payee, amount);
        return channelSettlementRequestDTO;
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
        CustomData primaryIdentifier = new CustomData();
        if (amsName.equalsIgnoreCase("paygops")) {
            foundationalId = paybillRequestDTO.getBillRefNo();
            primaryIdentifier.setKey("foundationalId");
            primaryIdentifier.setValue(foundationalId);
        } else if (amsName.equalsIgnoreCase("roster")) {
            accountID = paybillRequestDTO.getBillRefNo();
            primaryIdentifier.setKey("accountID");
            primaryIdentifier.setValue(accountID);
        }
        CustomData secondaryIdentifier = new CustomData();
        secondaryIdentifier.setKey("MSISDN");
        secondaryIdentifier.setValue(paybillRequestDTO.getMsisdn());
        // Mapping custom data
        List<CustomData> customData = setCustomDataChannelRequest(paybillRequestDTO, currency, foundationalId);

        ChannelRequestDTO channelRequestDTO = new ChannelRequestDTO();
        channelRequestDTO.setPrimaryIdentifier(primaryIdentifier);
        channelRequestDTO.setSecondaryIdentifier(secondaryIdentifier);
        channelRequestDTO.setCustomData(customData);

        return channelRequestDTO;
    }

    private static List<CustomData> setCustomDataChannelRequest(PaybillRequestDTO paybillRequestDTO, String currency, String foundationalId) {
        List<CustomData> customData = new ArrayList<>();

        CustomData transactionId = new CustomData();
        transactionId.setKey(TRANSACTION_ID);
        transactionId.setValue(paybillRequestDTO.getTransactionID());

        CustomData currencyObj = new CustomData();
        currencyObj.setKey(CURRENCY);
        currencyObj.setValue(currency);

        CustomData memo = new CustomData();
        memo.setKey(MEMO);
        memo.setValue(foundationalId);

        CustomData walletName = new CustomData();
        walletName.setKey(WALLET_NAME);
        walletName.setValue(paybillRequestDTO.getMsisdn());

        CustomData amount = new CustomData();
        amount.setKey(AMOUNT);
        amount.setValue(paybillRequestDTO.getTransactionAmount());

        customData.add(transactionId);
        customData.add(currencyObj);
        customData.add(memo);
        customData.add(walletName);
        customData.add(amount);
        return customData;
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