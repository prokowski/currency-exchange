package com.example.currencyexchange.account.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * A Spring Data JPA repository for managing {@link Account} entities.
 *
 * <p>This interface provides the necessary methods for persisting and retrieving the
 * {@code Account} aggregate root. It abstracts the underlying data storage details,
 * allowing the domain layer to remain persistence-ignorant. It uses {@link Long} as the
 * technical primary key and provides a method to query accounts by their
 * domain-specific identifier, {@link AccountId}.</p>
 */
@Repository
interface AccountRepository extends JpaRepository<Account, Long> {
    /**
     * Finds an account by its unique, domain-specific identifier.
     *
     * @param accountId The {@link AccountId} value object representing the business key of the account. Must not be null.
     * @return An {@link Optional} containing the found {@link Account}, or an empty {@code Optional} if no account with the given ID exists.
     */
    Optional<Account> findByAccountId(AccountId accountId);
}