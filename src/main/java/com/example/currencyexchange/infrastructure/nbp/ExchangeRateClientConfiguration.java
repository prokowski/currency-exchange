package com.example.currencyexchange.infrastructure.nbp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
class ExchangeRateClientConfiguration {

    @Value("${currencyexchange.nbp.api.url.template}")
    private String apiUrlTemplate;

    @Bean
    ExchangeRateClient exchangeRateClient(RestTemplate restTemplate) {
        return new ExchangeRateNbp(restTemplate, apiUrlTemplate);
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}