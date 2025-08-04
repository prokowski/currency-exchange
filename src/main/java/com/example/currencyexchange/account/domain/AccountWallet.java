package com.example.currencyexchange.account.domain;

import com.example.currencyexchange.account.domain.dto.AccountWalletView;
import com.example.currencyexchange.account.exception.InsufficientFundsException;
import com.example.currencyexchange.shared.ddd.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Represents a single currency wallet within a user's account.
 *
 * This entity is an integral part of the {@link Account} aggregate and should not be managed directly.
 * Its lifecycle and operations are controlled by the {@link Account} aggregate root to ensure consistency.
 * Its primary responsibilities are to hold the balance for a specific currency and to provide
 * safe methods for depositing and withdrawing funds, including checking for sufficient balance and respecting balance limits.
 */
@Entity
@Getter(AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
class AccountWallet extends AbstractEntity {

    // Define a constant for the maximum allowed balance to enforce the business rule in the domain.
    private static final BigDecimal MAX_BALANCE = new BigDecimal("9999999999.99");

    @NotNull
    @Size(max = 3)
    @Column(length = 3)
    private String currencyCode;

    @NotNull
    private BigDecimal balance;

    @Setter(AccessLevel.PACKAGE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_entity_id")
    private Account account;

    /**
     * Package-private constructor for creating a new wallet within the aggregate.
     */
    AccountWallet(String currencyCode, BigDecimal balance) {
        if (balance.compareTo(MAX_BALANCE) > 0) {
            throw new WalletBalanceLimitExceededException("Initial balance exceeds the maximum allowed limit of " + MAX_BALANCE);
        }
        this.currencyCode = currencyCode;
        this.balance = balance;
    }

    /**
     * Decreases the wallet balance by the given amount.
     * Encapsulates logic and validation.
     * @param amount The amount to withdraw.
     * @throws InsufficientFundsException if the balance is insufficient.
     */
    void withdraw(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds in account " + currencyCode + ". Available balance: " + balance);
        }
        this.balance = this.balance.subtract(amount);
    }

    /**
     * Increases the wallet balance by the given amount.
     * It also checks if the new balance would exceed the system's maximum limit.
     * @param amount The amount to deposit.
     * @throws WalletBalanceLimitExceededException if the resulting balance would exceed the allowed limit.
     */
    void deposit(BigDecimal amount) {
        BigDecimal newBalance = this.balance.add(amount);
        if (newBalance.compareTo(MAX_BALANCE) > 0) {
            throw new WalletBalanceLimitExceededException("Operation would cause the balance to exceed the maximum limit of " + MAX_BALANCE);
        }
        this.balance = newBalance;
    }

    /**
     * Checks if the wallet is assigned to the given currency code.
     * @param currencyCode The currency code to check.
     * @return true if the currency codes match.
     */
    boolean hasCurrency(String currencyCode) {
        return this.currencyCode.equalsIgnoreCase(currencyCode);
    }

    /**
     * Converts the wallet to its view representation (DTO).
     * @return An AccountWalletView object.
     */
    AccountWalletView toDto() {
        return AccountWalletView.builder()
                .currencyCode(currencyCode)
                .balance(balance)
                .build();
    }
}