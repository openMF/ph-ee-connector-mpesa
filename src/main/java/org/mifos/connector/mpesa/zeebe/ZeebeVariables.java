package org.mifos.connector.mpesa.zeebe;

public class ZeebeVariables {

    private ZeebeVariables() {
    }
    public static final String TRANSACTION_ID = "transactionId";
    public static final String SERVER_TRANSACTION_ID = "mpesaTransactionId";
    public static final String SERVER_TRANSACTION_RECEIPT_NUMBER = "mpesaReceiptNumber";
    public static final String CALLBACK_RECEIVED = "isCallbackReceived";
    public static final String CALLBACK = "callback";
    public static final String SERVER_TRANSACTION_STATUS_RETRY_COUNT = "mpesaTransactionStatusRetryCount";
    public static final String GET_TRANSACTION_STATUS_RESPONSE = "getTransactionStatusResponse";
    public static final String GET_TRANSACTION_STATUS_RESPONSE_CODE = "getTransactionStatusHttpCode";
    public static final String TRANSACTION_FAILED = "transactionFailed";
    public static final String TRANSFER_MESSAGE = "transaction-request";
    public static final String TRANSFER_RETRY_COUNT = "paymentTransferRetry";
    public static final String TRANSFER_RESPONSE_CREATE = "transferResponse-CREATE";
    public static final String TRANSFER_CREATE_FAILED = "transferCreateFailed";
    public static final String TIMER = "timer";
}
