package org.mifos.connector.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONObject;

import java.util.List;

//{
//        "requestingOrganisationTransactionReference": "string",
//        "subType": "inbound",
//        "type": "transfer",
//        "amount": "15.21",
//        "currency": "AED",
//        "descriptionText": "string",
//        "requestDate": "2022-09-28T12:51:19.260+00:00",
//        "customData": [
//            {
//            "key": "string",
//            "value": "string"
//            }
//            ],
//            "payer": [
//            {
//            "partyIdType": "msisdn",
//            "partyIdIdentifier": "+33555123456"
//            }
//            ],
//            "payee": [
//            {
//            "partyIdType": "accountid",
//            "partyIdIdentifier": "L7741025618"
//            }
//    ]
// }

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChannelRequestDTO {
    @JsonProperty("requestingOrganisationTransactionReference")
    private String requestingOrganisationTransactionReference;
    @JsonProperty("subType")
    private String subType;
    @JsonProperty("type")
    private String type;
    @JsonProperty("amount")
    private String amount;
    @JsonProperty("currency")
    private String currency;
    @JsonProperty("descriptionText")
    private String descriptionText;
    @JsonProperty("requestDate")
    private String requestDate;
    @JsonProperty("customData")
    private List<JSONObject> customData;
    @JsonProperty("payer")
    private List<JSONObject> payer;
    @JsonProperty("payee")
    private List<JSONObject> payee;
}
