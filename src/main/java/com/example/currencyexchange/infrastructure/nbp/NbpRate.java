package com.example.currencyexchange.infrastructure.nbp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class NbpRate {
    private BigDecimal mid;
}