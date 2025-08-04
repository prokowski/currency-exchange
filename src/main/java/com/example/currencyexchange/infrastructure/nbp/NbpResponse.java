package com.example.currencyexchange.infrastructure.nbp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class NbpResponse {
    private List<NbpRate> rates;
}