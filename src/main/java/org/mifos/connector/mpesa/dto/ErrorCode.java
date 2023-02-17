package org.mifos.connector.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorCode {

    @JsonProperty("id")
    int id;

    @JsonProperty("transactionType")
    String transactionType;

    @JsonProperty("errorMessage")
    String errorMessage;

    @JsonProperty("errorCode")
    String errorCode;

    @JsonProperty("recoverable")
    boolean recoverable;

    public int getId() {
        return id;
    }

}
