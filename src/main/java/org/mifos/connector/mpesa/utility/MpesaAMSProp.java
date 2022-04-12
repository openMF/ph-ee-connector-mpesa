package org.mifos.connector.mpesa.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class MpesaAMSProp {

    @Autowired
    private MpesaProps mpesaProps;

    List<MpesaProps.MPESA> mpesa;

    public MpesaAMSProp(){
    }


    public List<MpesaProps.MPESA> getGroup(){
        mpesa  =  mpesaProps.getAmsgroup();
        return mpesa;
    }

}

