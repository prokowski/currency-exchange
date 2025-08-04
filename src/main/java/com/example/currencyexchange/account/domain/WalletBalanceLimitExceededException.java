package com.example.currencyexchange.account.domain;

/**
 * A specific domain exception thrown when an operation would cause a wallet's balance
 * to exceed the maximum allowed value.
 */
class WalletBalanceLimitExceededException extends RuntimeException {
    WalletBalanceLimitExceededException(String message) {
        super(message);
    }

}