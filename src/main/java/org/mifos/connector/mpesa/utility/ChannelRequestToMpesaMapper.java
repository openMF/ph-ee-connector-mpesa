package org.mifos.connector.mpesa.utility;

import org.mifos.connector.common.channel.dto.TransactionChannelRequestDTO;
import org.mifos.connector.mpesa.dto.BuyGoodsPaymentRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class ChannelRequestToMpesaMapper implements
        EntityMapper<TransactionChannelRequestDTO, BuyGoodsPaymentRequestDTO> {

    private String callbackUrl = "http://localhost:5000/buygoods/callback";

    @Override
    public BuyGoodsPaymentRequestDTO fromEntityToDomain(TransactionChannelRequestDTO transactionChannelRequestDTO) {
        BuyGoodsPaymentRequestDTO buyGoodsPaymentRequestDTO = new BuyGoodsPaymentRequestDTO();
        buyGoodsPaymentRequestDTO.setCallBackURL(callbackUrl);
        return new BuyGoodsPaymentRequestDTO();
    }

    @Override
    public TransactionChannelRequestDTO fromDomainToEntity(BuyGoodsPaymentRequestDTO buyGoodsPaymentRequestDTO) {
        return new TransactionChannelRequestDTO();
    }

}
