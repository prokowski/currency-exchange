package com.example.currencyexchange.account.domain;

import com.example.currencyexchange.account.domain.dto.AccountWalletView;
import com.example.currencyexchange.account.exception.InsufficientFundsException;
import com.example.currencyexchange.shared.ddd.AbstractEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a single currency wallet within a user's account.
 *
 * <p>This entity is a fundamental part of the {@link Account} aggregate and is not intended for direct management.
 * Its lifecycle and all operations are controlled by the {@link Account} aggregate root to maintain data consistency.
 * The wallet's primary role is to manage the balance for a specific currency and provide secure methods for
 * deposits and withdrawals.</p>
 */
@Entity
@Getter(AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
class AccountWallet extends AbstractEntity {

    /**
     * The balance of the wallet, including the amount and currency.
     */
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "balance", nullable = false)),
            @AttributeOverride(name = "currency.code", column = @Column(name = "currencyCode", nullable = false, length = 3))
    })
    private Money balance;

    /**
     * A reference back to the parent Account aggregate root.
     */
    @Setter(AccessLevel.PACKAGE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_entity_id")
    private Account account;

    /**
     * Creates a new wallet with an initial balance.
     * This constructor should only be called from within the {@link Account} aggregate.
     *
     * @param initialBalance The starting balance for the new wallet.
     */
    AccountWallet(Money initialBalance) {
        this.balance = initialBalance;
    }

    /**
     * Decreases the wallet balance by the specified amount.
     *
     * @param amount The {@link Money} object representing the amount to withdraw.
     * @throws IllegalArgumentException if the currency of the amount does not match the wallet's currency.
     * @throws InsufficientFundsException if the wallet does not have enough funds.
     */
    void withdraw(Money amount) {
        if (!this.balance.currency().equals(amount.currency())) {
            throw new IllegalArgumentException("Cannot withdraw a different currency from the wallet.");
        }
        if (this.balance.isLessThan(amount)) {
            throw new InsufficientFundsException("Insufficient funds in " + balance.currency().code() + " wallet. Available balance: " + balance.amount());
        }
        this.balance = this.balance.subtract(amount);
    }

    /**
     * Increases the wallet balance by the specified amount.
     *
     * @param amount The {@link Money} object representing the amount to deposit.
     * @throws IllegalArgumentException if the currency of the amount does not match the wallet's currency.
     */
    void deposit(Money amount) {
        if (!this.balance.currency().equals(amount.currency())) {
            throw new IllegalArgumentException("Cannot deposit a different currency into the wallet.");
        }
        this.balance = this.balance.add(amount);
    }

    /**
     * Checks if the wallet holds funds in the specified currency.
     *
     * @param currency The {@link Currency} to check against.
     * @return {@code true} if the wallet's currency matches the specified currency, otherwise {@code false}.
     */
    boolean hasCurrency(Currency currency) {
        return this.balance.currency().equals(currency);
    }

    /**
     * Converts the {@code AccountWallet} entity into its data transfer object (DTO) representation.
     *
     * @return An {@link AccountWalletView} containing the wallet's public data.
     */
    AccountWalletView toDto() {
        return AccountWalletView.builder()
                .currencyCode(balance.currency().code())
                .balance(balance.amount())
                .build();
    }
}