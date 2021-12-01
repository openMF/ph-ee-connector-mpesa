package org.mifos.connector.mpesa.zeebe;

public class ZeebeVariables {

    private ZeebeVariables() {
    }
    public static final String TRANSACTION_ID = "transactionId";
    public static final String SERVER_TRANSACTION_ID = "mpesaTransactionId";
    public static final String TRANSACTION_FAILED = "transactionFailed";
    public static final String TRANSFER_MESSAGE = "transaction-request";
    public static final String TRANSFER_RETRY_COUNT = "paymentTransferRetry";
}
