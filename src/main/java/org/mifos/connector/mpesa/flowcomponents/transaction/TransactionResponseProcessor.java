package org.mifos.connector.mpesa.flowcomponents.transaction;

import com.google.gson.Gson;
import org.apache.camel.Exchange;
import org.apache.camel.util.json.JsonObject;
import org.springframework.stereotype.Component;
import org.apache.camel.Processor;

import static org.mifos.connector.mpesa.camel.config.CamelProperties.*;
import static org.mifos.connector.mpesa.zeebe.ZeebeVariables.*;

@Component
public class TransactionResponseProcessor implements Processor{

    @Override
    public void process(Exchange exchange) {

        Object hasTransferFailed = exchange.getProperty(TRANSACTION_FAILED);

        if (hasTransferFailed != null && (boolean)hasTransferFailed) {
            String body = exchange.getIn().getBody(String.class);
            exchange.setProperty(TRANSACTION_FAILED, true);
            exchange.setProperty(ERROR_INFORMATION, body);
            exchange.setProperty(ERROR_CODE, getErrorCode(body));
            exchange.setProperty(ERROR_DESCRIPTION, getErrorDescription(body));
        } else {
            exchange.setProperty(TRANSACTION_FAILED, false);
            exchange.setProperty(SERVER_TRANSACTION_ID, exchange.getProperty(SERVER_TRANSACTION_ID));
        }

    }

    /**
     * Sample Error Json payload
     * {
     *    "requestId":"22749-38515563-2",
     *    "errorCode":"404.001.03",
     *    "errorMessage":"Invalid Access Token"
     * }
     * @param errorJson
     * @return errorCode
     */
    private String getErrorCode(String errorJson) {
        String errorCode;
        JsonObject jsonObject = (new Gson()).fromJson(errorJson, JsonObject.class);
        errorCode = jsonObject.getString("errorCode");
        return errorCode;
    }

    /**
     * Sample Error Json payload
     * {
     *    "requestId":"22749-38515563-2",
     *    "errorCode":"404.001.03",
     *    "errorMessage":"Invalid Access Token"
     * }
     * @param errorJson
     * @return errorDescription
     */
    private String getErrorDescription(String errorJson) {
        String errorDescription;
        JsonObject jsonObject = (new Gson()).fromJson(errorJson, JsonObject.class);
        errorDescription = jsonObject.getString("errorMessage");
        return errorDescription;
    }

}
