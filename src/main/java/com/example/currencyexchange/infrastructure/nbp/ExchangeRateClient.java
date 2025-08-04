package com.example.currencyexchange.infrastructure.nbp;

import java.math.BigDecimal;

/**
 * Defines a contract for any client responsible for fetching currency exchange rates.
 * This interface decouples the application's domain logic from the specific implementation
 * of an external exchange rate provider (e.g., NBP API, another third-party service).
 */
public interface ExchangeRateClient {
    /**
     * Retrieves the exchange rate between two currencies.
     *
     * @param fromCurrency The source currency code (e.g., "USD").
     * @param toCurrency   The target currency code (e.g., "PLN").
     * @return The exchange rate as a {@link BigDecimal}.
     */
    BigDecimal getExchangeRate(String fromCurrency, String toCurrency);
}