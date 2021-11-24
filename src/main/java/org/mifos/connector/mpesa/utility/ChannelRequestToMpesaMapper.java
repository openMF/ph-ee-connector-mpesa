package org.mifos.connector.mpesa.utility;

import org.mifos.connector.common.channel.dto.TransactionChannelRequestDTO;
import org.mifos.connector.mpesa.dto.BuyGoodsPaymentRequestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChannelRequestToMpesaMapper implements
        EntityMapper<TransactionChannelRequestDTO, BuyGoodsPaymentRequestDTO> {

    @Value("${mpesa.local.host}")
    private String localhost;

    @Value("${mpesa.local.host}")
    private String callbackEndpoint;

    @Override
    public BuyGoodsPaymentRequestDTO fromEntityToDomain(TransactionChannelRequestDTO transactionChannelRequestDTO) {
        BuyGoodsPaymentRequestDTO buyGoodsPaymentRequestDTO = new BuyGoodsPaymentRequestDTO();
        buyGoodsPaymentRequestDTO.setCallBackURL(localhost+callbackEndpoint);
        return new BuyGoodsPaymentRequestDTO();
    }

    @Override
    public TransactionChannelRequestDTO fromDomainToEntity(BuyGoodsPaymentRequestDTO buyGoodsPaymentRequestDTO) {
        return new TransactionChannelRequestDTO();
    }

}
