package org.mifos.connector.mpesa.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.mifos.connector.common.gsma.dto.CustomData;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChannelRequestDTO {
    @JsonProperty("primaryIdentifier")
    private CustomData primaryIdentifier;
    @JsonProperty("secondaryIdentifier")
    private CustomData secondaryIdentifier;
    @JsonProperty("customData")
    private List<CustomData> customData;
}
