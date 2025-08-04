package com.example.currencyexchange.account.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class AccountView {
    private String accountId;
    private String firstName;
    private String lastName;
    private List<AccountWalletView> balances;
}