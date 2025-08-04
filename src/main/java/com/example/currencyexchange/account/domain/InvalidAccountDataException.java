package com.example.currencyexchange.account.domain;

/**
 * A specific domain exception thrown when data provided for account creation
 * violates business rules (e.g., blank name, non-positive balance).
 */
class InvalidAccountDataException extends RuntimeException {
    InvalidAccountDataException(String message) {
        super(message);
    }
}
