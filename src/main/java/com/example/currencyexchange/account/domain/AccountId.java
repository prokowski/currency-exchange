package com.example.currencyexchange.account.domain;

import jakarta.persistence.Embeddable;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the unique identifier for an {@link Account} as a Value Object.
 *
 * <p>This class encapsulates a string-based UUID, providing a type-safe way to
 * represent an account's identity throughout the domain. As a Value Object, it is
 * immutable and its equality is based on its value, not its reference.</p>
 *
 * <p>It implements {@link Serializable} to support being part of entities that
 * might be serialized, for example, in caching or distributed scenarios.</p>
 */
@Embeddable
record AccountId(@NonNull String value) implements Serializable {

    /**
     * Compact constructor to ensure the value is not null upon creation.
     *
     * @param value The string representation of the account ID.
     * @throws NullPointerException if the value is null.
     */
    AccountId {
        Objects.requireNonNull(value, "Account ID value cannot be null.");
    }

    /**
     * A static factory method to generate a new, unique {@code AccountId}.
     *
     * @return A new {@code AccountId} instance with a randomly generated UUID value.
     */
    static AccountId generate() {
        return new AccountId(UUID.randomUUID().toString());
    }
}