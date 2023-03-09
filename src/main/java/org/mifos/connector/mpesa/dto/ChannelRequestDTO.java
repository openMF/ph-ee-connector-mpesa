package org.mifos.connector.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.mifos.connector.common.gsma.dto.CustomData;

import java.util.List;

//{
//        "primaryIdentifier": {
//        "key": "foundationalID",
//        "value": "24322607"
//        },
//        "secondaryIdentifier": {
//        "key": "MSISDN",
//        "value": "254797668592"
//        },
//        "customData": [
//        {
//        "key": "transactionId",
//        "value": "670d65bd-4efd-4a6c-ae2c-7fdaa8cb4d60"
//        },
//        {
//        "key": "currency",
//        "value": "KES"
//        },
//        {
//        "key": "memo",
//        "value": "24322607"
//        },
//        {
//        "key": "wallet_name",
//        "value": "254797668592"
//        },
//        {
//        "key": "amount",
//        "value": "11"
//        }
//        ]
//        }

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChannelRequestDTO {
    @JsonProperty("primaryIdentifier")
    private PrimarySecondaryIdentifier primaryIdentifier;
    @JsonProperty("secondaryIdentifier")
    private PrimarySecondaryIdentifier secondaryIdentifier;
    @JsonProperty("customData")
    private List<CustomData> customData;
}
