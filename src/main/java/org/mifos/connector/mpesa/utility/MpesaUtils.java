package org.mifos.connector.mpesa.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class MpesaUtils {

    @Autowired
    private MpesaAMSProp mpesaAMSProp;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<MpesaProps.MPESA> mpesa;

    private String process = "process";

    public String getProcess(){
        return process;
    }

    public List<MpesaProps.MPESA> getGroup(){
       mpesa = mpesaAMSProp.getGroup();
       return mpesa;
    }

    public MpesaProps.MPESA getMpesaProperties(){
        MpesaProps.MPESA properties = null;
        List<MpesaProps.MPESA> groups = getGroup();
        for(MpesaProps.MPESA identifier : groups){
            String name = identifier.getName();
            String process = getProcess();
            if (process.contains(name)) {
                properties = identifier;
                break;

            }
            else {
                if(name.equals("default")) {
                   properties = identifier;
                }
            }
        }
        return properties;
    }


    public void setProcess(String process) {
        logger.info("Process Value being set");
        this.process = process;
    }

    public static String maskString(String strText) {

        char maskChar = '*';
        int start = 0;
        int end = strText.length() - 4;

        if (start > end){
            return "***";
        }

        int maskLength = end - start;

        if(maskLength == 0)
            return strText;

        StringBuilder sbMaskString = new StringBuilder(maskLength);

        for(int i = 0; i < maskLength; i++){
            sbMaskString.append(maskChar);
        }

        return strText.substring(0, start) + sbMaskString + strText.substring(start + maskLength);
    }

    public static void main(String[] args)  {
        String dt = "254708374149";
        System.out.println(maskString(dt));
    }

}