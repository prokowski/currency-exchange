package com.example.currencyexchange.account.domain;

import lombok.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Represents the exchange rate between two currencies as a Value Object.
 *
 * <p>This class encapsulates the source currency ({@code from}), the target currency ({@code to}),
 * and the rate of exchange. As a Value Object, it is immutable and its primary role is to
 * perform currency conversion calculations in a type-safe manner.</p>
 */
record ExchangeRate(@NonNull Currency from, @NonNull Currency to, @NonNull BigDecimal rate) {

    /**
     * Exchanges a given amount of money from the source currency to the target currency.
     *
     * <p>This method calculates the resulting amount by multiplying the input money's amount
     * by the exchange rate. The result is scaled to two decimal places using half-up rounding.</p>
     *
     * @param money The {@link Money} object to be exchanged. Its currency must match the 'from' currency of this rate.
     * @return A new {@link Money} object representing the exchanged amount in the 'to' currency.
     * @throws IllegalArgumentException if the currency of the input money does not match the 'from' currency of the rate.
     */
    Money exchange(@NonNull Money money) {
        if (!money.currency().equals(from)) {
            throw new IllegalArgumentException("The money to be exchanged must be in the 'from' currency of the rate.");
        }
        BigDecimal exchangedAmount = money.amount().multiply(rate).setScale(2, RoundingMode.HALF_UP);
        return new Money(exchangedAmount, to);
    }
}