package com.example.currencyexchange.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Represents a currency supported by the application for exchange operations.
 * This entity acts as a simple lookup table (e.g., populated via Flyway)
 * to validate whether a given currency code is allowed before performing an exchange.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
class SupportedCurrency {

    @Id
    @Size(max = 3)
    @Column(length = 3)
    private String currencyCode;
}