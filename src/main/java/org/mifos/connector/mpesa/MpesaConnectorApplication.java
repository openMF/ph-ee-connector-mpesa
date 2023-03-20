package org.mifos.connector.mpesa;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.camel.Processor;
import org.mifos.connector.mpesa.camel.config.CustomHeaderFilterStrategy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import static org.mifos.connector.mpesa.camel.config.CamelProperties.CUSTOM_HEADER_FILTER_STRATEGY;

@SpringBootApplication
public class MpesaConnectorApplication {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    public Processor pojoToString(ObjectMapper objectMapper) {
        return exchange -> exchange.getIn().setBody(objectMapper.writeValueAsString(exchange.getIn().getBody()));
    }

    @Bean(CUSTOM_HEADER_FILTER_STRATEGY)
    public CustomHeaderFilterStrategy headerFilterStrategy() {
        return new CustomHeaderFilterStrategy();
    }

    public static void main(String[] args) {
        SpringApplication.run(MpesaConnectorApplication.class, args);
    }

}
