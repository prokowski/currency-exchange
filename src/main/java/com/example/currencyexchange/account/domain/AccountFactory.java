package com.example.currencyexchange.account.domain;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;

/**
 * A factory for creating {@link Account} aggregate roots.
 *
 * <p>This class encapsulates the logic for constructing a new, valid {@code Account} object.
 * It ensures that every new account is initialized with a valid name, a positive initial
 * balance in PLN, and a corresponding wallet. By centralizing the creation logic, it
 * promotes consistency and adheres to the principles of Domain-Driven Design.</p>
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class AccountFactory {

    /**
     * Creates and initializes a new {@link Account} aggregate.
     *
     * <p>This method constructs the necessary Value Objects ({@link PersonName}, {@link Money}),
     * generates a unique {@link AccountId}, and assembles the {@code Account} aggregate.
     * It also creates the initial PLN wallet with the specified balance.</p>
     *
     * @param firstName         The user's first name. Must not be null.
     * @param lastName          The user's last name. Must not be null.
     * @param initialBalancePLN The initial balance in PLN. Must be a positive value.
     * @return A fully initialized, valid {@link Account} aggregate, ready to be persisted.
     * @throws InvalidAccountDataException if the initial balance is not greater than zero.
     */
    Account create(@NonNull String firstName, @NonNull String lastName, @NonNull BigDecimal initialBalancePLN) {
        PersonName personName = new PersonName(firstName, lastName);

        if (initialBalancePLN.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAccountDataException("Initial balance must be greater than zero.");
        }

        Money initialMoney = new Money(initialBalancePLN, new Currency("PLN"));

        Account account = Account.builder()
                .accountId(AccountId.generate())
                .personName(personName)
                .accountWallets(new HashSet<>())
                .build();

        account.addWallet(new AccountWallet(initialMoney));
        return account;
    }
}