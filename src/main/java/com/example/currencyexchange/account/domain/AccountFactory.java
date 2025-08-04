package com.example.currencyexchange.account.domain;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.UUID;

/**
 * A factory class responsible for creating {@link Account} aggregates.
 *
 * It encapsulates the complexity of object creation, ensuring that every new account
 * is instantiated in a consistent and valid state. This includes generating a unique
 * account ID, validating input data, and creating an initial wallet with a PLN balance.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class AccountFactory {

    private final static String INITIAL_CURRENCY_CODE = "PLN";

    Account create(@NonNull String firstName, @NonNull String lastName, @NonNull BigDecimal initialBalancePLN) {
        validateName(firstName, "First name");
        validateName(lastName, "Last name");
        validateInitialBalance(initialBalancePLN);

        Account account = Account.builder()
                .accountId(UUID.randomUUID().toString())
                .firstName(firstName)
                .lastName(lastName)
                .accountWallets(new HashSet<>())
                .build();

        account.addWallet(new AccountWallet(INITIAL_CURRENCY_CODE, initialBalancePLN));
        return account;
    }

    private void validateName(String name, String fieldName) {
        if (name == null || name.isBlank()) {
            throw new InvalidAccountDataException(fieldName + " is required.");
        }
        if (name.length() > 50) {
            throw new InvalidAccountDataException(fieldName + " cannot exceed 50 characters.");
        }
    }

    private void validateInitialBalance(BigDecimal balance) {
        if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAccountDataException("Initial balance must be greater than zero.");
        }
    }
}