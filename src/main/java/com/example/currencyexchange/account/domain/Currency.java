package com.example.currencyexchange.account.domain;

import jakarta.persistence.Embeddable;
import lombok.NonNull;

import java.util.Objects;

/**
 * Represents a currency as a Value Object, ensuring the currency code is valid.
 *
 * <p>This class encapsulates a three-letter currency code (e.g., "PLN", "USD"),
 * providing a type-safe way to handle currencies throughout the domain. As a Value
 * Object, it is immutable and its equality is based on its value.</p>
 *
 * <p>It enforces that the currency code is a non-null, three-character string,
 * and automatically converts it to uppercase to ensure consistency.</p>
 */
@Embeddable
record Currency(@NonNull String code) {

    /**
     * Compact constructor for validation and normalization.
     *
     * @param code The three-letter currency code.
     * @throws NullPointerException if the code is null.
     * @throws IllegalArgumentException if the code is not exactly three characters long or is blank.
     */
    Currency {
        Objects.requireNonNull(code, "Currency code cannot be null.");
        if (code.isBlank() || code.length() != 3) {
            throw new IllegalArgumentException("Invalid currency code. Must be 3 characters long.");
        }
        // Normalize the code to uppercase for consistent comparisons.
        code = code.toUpperCase();
    }
}