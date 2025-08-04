package com.example.currencyexchange.account.domain;

import jakarta.persistence.Embeddable;
import lombok.NonNull;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a specific amount of money in a particular currency as a Value Object.
 *
 * <p>This class is a fundamental building block in the domain, providing a type-safe
 * and immutable way to handle monetary values. It encapsulates a {@link BigDecimal} amount
 * and a {@link Currency}. As a Value Object, its equality is based on its values, not
 * on its reference.</p>
 *
 * <p>It enforces several business rules upon creation:
 * <ul>
 * <li>The amount cannot be negative.</li>
 * <li>The amount cannot exceed a predefined maximum balance limit.</li>
 * </ul>
 * It also provides safe methods for arithmetic operations (add, subtract) and comparisons,
 * ensuring that operations are only performed on money of the same currency.</p>
 */
@Embeddable
record Money(@NonNull BigDecimal amount, @NonNull Currency currency) {

    private static final BigDecimal MAX_BALANCE = new BigDecimal("9999999999.99");

    /**
     * Compact constructor that validates the money's state upon creation.
     *
     * @throws NullPointerException if amount or currency is null.
     * @throws IllegalArgumentException if the amount is negative.
     * @throws WalletBalanceLimitExceededException if the amount exceeds the system's maximum allowed balance.
     */
    Money {
        Objects.requireNonNull(amount, "Amount cannot be null.");
        Objects.requireNonNull(currency, "Currency cannot be null.");
        if (amount.signum() < 0) { //
            throw new IllegalArgumentException("Amount cannot be negative."); //
        }
        if (amount.compareTo(MAX_BALANCE) > 0) { //
            throw new WalletBalanceLimitExceededException("Amount exceeds the maximum allowed limit of " + MAX_BALANCE); //
        }
    }

    /**
     * Adds another {@code Money} object to this one.
     *
     * @param other The {@code Money} object to add. Must be of the same currency.
     * @return A new {@code Money} object representing the sum.
     * @throws IllegalArgumentException if the currencies do not match.
     */
    Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money in different currencies.");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    /**
     * Subtracts another {@code Money} object from this one.
     *
     * @param other The {@code Money} object to subtract. Must be of the same currency.
     * @return A new {@code Money} object representing the difference.
     * @throws IllegalArgumentException if the currencies do not match or if funds are insufficient for the subtraction.
     */
    Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot subtract money in different currencies.");
        }
        if (this.amount.compareTo(other.amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for subtraction.");
        }
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * Checks if this {@code Money} object's amount is greater than another's.
     *
     * @param other The {@code Money} object to compare against. Must be of the same currency.
     * @return {@code true} if this amount is greater, otherwise {@code false}.
     * @throws IllegalArgumentException if the currencies do not match.
     */
    boolean isGreaterThan(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare money in different currencies.");
        }
        return this.amount.compareTo(other.amount) > 0;
    }

    /**
     * Checks if this {@code Money} object's amount is less than another's.
     *
     * @param other The {@code Money} object to compare against. Must be of the same currency.
     * @return {@code true} if this amount is less, otherwise {@code false}.
     * @throws IllegalArgumentException if the currencies do not match.
     */
    boolean isLessThan(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot compare money in different currencies.");
        }
        return this.amount.compareTo(other.amount) < 0;
    }

    /**
     * Checks if the amount is zero.
     *
     * @return {@code true} if the amount is exactly zero, otherwise {@code false}.
     */
    boolean isZero() {
        return this.amount.signum() == 0;
    }

    /**
     * Checks if the amount is positive (greater than zero).
     *
     * @return {@code true} if the amount is greater than zero, otherwise {@code false}.
     */
    boolean isPositive() {
        return this.amount.signum() > 0;
    }
}