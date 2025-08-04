package com.example.currencyexchange.account.domain;

import com.example.currencyexchange.account.domain.dto.AccountView;
import com.example.currencyexchange.account.exception.InsufficientFundsException;
import com.example.currencyexchange.shared.ddd.AbstractAggregateEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Represents the <strong>Account</strong> aggregate root.
 *
 * <p>This entity is the core of the account domain, managing user information and a collection
 * of {@link AccountWallet}s for different currencies. As an aggregate root, it enforces all
 * business rules and ensures the consistency of the account and its associated wallets.
 * All modifications must be performed through this class.</p>
 */
@Entity
@Builder
@Getter(AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
class Account extends AbstractAggregateEntity {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "accountId", unique = true, nullable = false))
    private AccountId accountId;

    @Embedded
    private PersonName personName;

    @Builder.Default
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<AccountWallet> accountWallets = new HashSet<>();

    /**
     * Executes a currency exchange between two wallets within the account.
     *
     * <p>This method debits the source currency wallet and credits the target currency wallet
     * based on the provided exchange rate. If the target wallet does not exist, it will be
     * created automatically.</p>
     *
     * @param amountToExchange The amount and currency to be exchanged.
     * @param rate The exchange rate to apply.
     * @throws InsufficientFundsException if the source wallet has insufficient funds or does not exist.
     */
    void exchange(Money amountToExchange, ExchangeRate rate) {
        AccountWallet fromWallet = findWalletByCurrency(amountToExchange.currency())
                .orElseThrow(() -> new InsufficientFundsException("No funds in currency " + amountToExchange.currency().code() + ". Please make a prior exchange or top up the account."));

        Money amountToReceive = rate.exchange(amountToExchange);
        AccountWallet toWallet = findOrCreateWalletByCurrency(amountToReceive.currency());

        fromWallet.withdraw(amountToExchange);
        toWallet.deposit(amountToReceive);
    }

    /**
     * Adds a new {@link AccountWallet} to the account.
     *
     * <p>This method ensures the bidirectional relationship between the account and the wallet
     * is correctly established.</p>
     *
     * @param accountWallet The wallet to be added to the account.
     */
    void addWallet(AccountWallet accountWallet) {
        accountWallets.add(accountWallet);
        accountWallet.setAccount(this);
    }

    /**
     * Converts the {@code Account} entity into its data transfer object (DTO) representation.
     *
     * @return An {@link AccountView} containing the account's data.
     */
    AccountView toDto() {
        return AccountView.builder()
                .accountId(accountId.value())
                .firstName(personName.firstName())
                .lastName(personName.lastName())
                .balances(accountWallets.stream()
                        .map(AccountWallet::toDto).collect(Collectors.toList()))
                .build();
    }

    /**
     * Finds a wallet associated with the specified currency.
     *
     * @param currency The currency of the wallet to find.
     * @return An {@link Optional} containing the {@link AccountWallet} if found, otherwise an empty {@code Optional}.
     */
    private Optional<AccountWallet> findWalletByCurrency(Currency currency) {
        return accountWallets.stream()
                .filter(wallet -> wallet.hasCurrency(currency))
                .findFirst();
    }

    /**
     * Finds an existing wallet for the specified currency or creates a new one if it does not exist.
     *
     * <p>A new wallet is initialized with a zero balance.</p>
     *
     * @param currency The currency of the wallet to find or create.
     * @return The existing or newly created {@link AccountWallet}.
     */
    private AccountWallet findOrCreateWalletByCurrency(Currency currency) {
        return findWalletByCurrency(currency)
                .orElseGet(() -> {
                    AccountWallet newWallet = new AccountWallet(new Money(BigDecimal.ZERO, currency));
                    this.addWallet(newWallet);
                    return newWallet;
                });
    }
}