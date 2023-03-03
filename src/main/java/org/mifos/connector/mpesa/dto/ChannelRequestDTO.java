package org.mifos.connector.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChannelRequestDTO {
    @JsonProperty("primaryIdentifier")
    private JSONObject primaryIdentifier;
    @JsonProperty("secondaryIdentifier")
    private JSONObject secondaryIdentifier;
    @JsonProperty("customData")
    private List<JSONObject> customData;
}
