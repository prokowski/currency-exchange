package com.example.currencyexchange.account.domain;

import com.example.currencyexchange.account.domain.dto.AccountView;
import com.example.currencyexchange.account.exception.InsufficientFundsException;
import com.example.currencyexchange.shared.ddd.AbstractAggregateEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Represents the Account aggregate root in the domain model.
 *
 * This class is the central entity for all account-related operations. It encapsulates the user's
 * personal information (first name, last name) and a collection of {@link AccountWallet}s,
 * which hold balances in different currencies. As an aggregate root, it is responsible for
 * maintaining its own consistency and enforcing all business rules related to its state and
 * the state of its child entities (wallets). All operations that modify an account or its
 * wallets should go through this class.
 */
@Entity
@Builder
@Getter(AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
class Account extends AbstractAggregateEntity {

    @NotNull
    @Column(unique = true)
    private String accountId;

    @NotNull
    @Size(max = 50)
    @Column(length = 50)
    private String firstName;

    @NotNull
    @Size(max = 50)
    @Column(length = 50)
    private String lastName;

    @Builder.Default
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<AccountWallet> accountWallets = new HashSet<>();

    /**
     * The main business method of the aggregate, responsible for currency exchange.
     * It coordinates operations on the wallets belonging to this account.
     * @param fromCurrency The source currency code.
     * @param toCurrency The target currency code.
     * @param amount The amount to exchange.
     * @param rate The exchange rate.
     */
    void exchange(String fromCurrency, String toCurrency, BigDecimal amount, BigDecimal rate) {
        AccountWallet fromWallet = findWalletByCurrency(fromCurrency)
                .orElseThrow(() -> new InsufficientFundsException("No funds in currency " + fromCurrency + ". Please make a prior exchange or top up the account."));

        AccountWallet toWallet = findOrCreateWalletByCurrency(toCurrency);

        fromWallet.withdraw(amount);

        BigDecimal amountToReceive = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        toWallet.deposit(amountToReceive);
    }

    /**
     * Adds a wallet to the account's set of wallets and sets the back-reference.
     * This method ensures the integrity of the aggregate.
     * @param accountWallet The wallet to add.
     */
    void addWallet(AccountWallet accountWallet) {
        accountWallets.add(accountWallet);
        accountWallet.setAccount(this);
    }

    /**
     * Converts the Account aggregate to its view representation (DTO).
     * @return An AccountView object.
     */
    AccountView toDto() {
        return AccountView.builder()
                .accountId(accountId)
                .firstName(firstName)
                .lastName(lastName)
                .balances(accountWallets.stream()
                        .map(AccountWallet::toDto).collect(Collectors.toList()))
                .build();
    }

    /**
     * Finds a wallet by its currency code.
     * @param currencyCode The currency code to search for.
     * @return An Optional containing the wallet if found.
     */
    private Optional<AccountWallet> findWalletByCurrency(String currencyCode) {
        return accountWallets.stream()
                .filter(wallet -> wallet.hasCurrency(currencyCode))
                .findFirst();
    }

    /**
     * Finds a wallet for the given currency or creates a new one with a zero balance if it doesn't exist.
     * @param currencyCode The currency code for the wallet.
     * @return The existing or newly created wallet.
     */
    private AccountWallet findOrCreateWalletByCurrency(String currencyCode) {
        return findWalletByCurrency(currencyCode)
                .orElseGet(() -> {
                    AccountWallet newWallet = new AccountWallet(currencyCode, BigDecimal.ZERO);
                    this.addWallet(newWallet);
                    return newWallet;
                });
    }
}