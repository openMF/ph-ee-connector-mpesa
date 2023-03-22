package org.mifos.connector.mpesa.camel.config;

public class CamelProperties {

    public CamelProperties() {
    }

    public static final String BUY_GOODS_REQUEST_BODY = "buyGoodsRequestBody";
    public static final String BUY_GOODS_TRANSACTION_STATUS_BODY = "buyGoodsTransactionStatusBody";
    public static final String ACCESS_TOKEN = "accessToken";
    public static final String TRANSACTION_TYPE = "transactionType";
    public static final String ERROR_INFORMATION = "errorInformation";
    public static final String TRANSACTION_ID = "transactionId";
    public static final String CHANNEL_REQUEST = "channelRequest";
    public static final String CORRELATION_ID = "correlationId";
    public static final String STATUS_AVAILABLE = "statusAvailable";
    public static final String TRANSACTION_STATUS = "transactionStatus";
    public static final String MPESA_API_REQUEST_FAILED = "mpesaApiCallFailed";
    public static final String IS_RETRY_EXCEEDED = "isRetryExceeded";
    public static final String IS_TRANSACTION_PENDING = "isTransactionPending";
    public static final String ZEEBE_ELEMENT_INSTANCE_KEY = "elementInstanceKey";
    public static final String MPESA_API_RESPONSE = "mpesaApiResponse";
    public static final String OPERATIONS_FILTER_BY = "operationsFilterBy";
    public static final String OPERATIONS_FILTER_VALUE = "operationsFilterValue";
    public static final String ERROR_CODE = "errorCode";
    public static final String ERROR_DESCRIPTION = "errorDescription";
    public static final String IS_ERROR_RECOVERABLE = "isErrorRecoverable";
    public static final String LAST_RESPONSE_BODY = "lastResponseBody";
    public static final String DEPLOYED_PROCESS = "deployedProcess";
    public static final String CUSTOM_HEADER_FILTER_STRATEGY = "customHeaderFilterStrategy";
    public static final String CURRENCY = "currency";
    public static final String MEMO = "memo";
    public static final String WALLET_NAME = "wallet_name";
    public static final String AMOUNT = "amount";
    public static final String AMS_NAME = "amsName";
    public static final String TENANT_ID = "Platform-TenantId";
    public static final String CONTENT_TYPE_VAL = "application/json";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String RECONCILED = "reconciled";
    public static final String MPESA_TXN_ID = "mpesaTxnId";
    public static final String CLIENT_CORRELATION_ID = "X-CorrelationID";
    public static final String ACCOUNT_HOLDING_INSTITUTION_ID = "accountHoldingInstitutionId";
}
