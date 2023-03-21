package org.mifos.connector.mpesa.utility;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ShortCodeAms {
    String businessShortCode;
    String ams;
    String currency;
}
