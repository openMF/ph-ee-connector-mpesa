package org.mifos.connector.mpesa.zeebe;

public class ZeebeVariables {

    private ZeebeVariables() {
    }
    public static final String TRANSACTION_ID = "transactionId";
    public static final String IS_VALID_TRANSACTION = "isValidTransaction";
    public static final String VALIDATION_RESPONSE = "validationResponse";
    public static final String TRANSACTION_FAILED = "transactionFailed";
    public static final String TRANSFER_MESSAGE = "transfer-message";
    public static final String TRANSFER_RESPONSE = "transferResponse";
    public static final String TRANSFER_RETRY_COUNT = "paymentTransferRetry";
}
