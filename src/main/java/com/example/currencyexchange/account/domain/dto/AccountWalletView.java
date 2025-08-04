package com.example.currencyexchange.account.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class AccountWalletView {

    private String currencyCode;

    private BigDecimal balance;
}
