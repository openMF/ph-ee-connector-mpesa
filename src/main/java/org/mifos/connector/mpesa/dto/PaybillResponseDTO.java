package org.mifos.connector.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaybillResponseDTO {
    @JsonProperty("reconciled")
    private boolean reconciled;

    @JsonProperty("amsName")
    private String amsName;

    @JsonProperty("accountHoldingInstitutionId")
    private String accountHoldingInstitutionId;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("msisdn")
    private String msisdn;
}
