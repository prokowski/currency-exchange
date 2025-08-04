package com.example.currencyexchange.account.domain;

import com.example.currencyexchange.account.domain.dto.AccountView;
import com.example.currencyexchange.account.domain.dto.CreateAccountRequest;
import com.example.currencyexchange.account.domain.dto.ExchangeRequest;
import com.example.currencyexchange.account.exception.AccountNotFoundException;
import com.example.currencyexchange.account.exception.InvalidCurrencyException;
import com.example.currencyexchange.infrastructure.nbp.ExchangeRateClient;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * The AccountFacade provides a simplified, high-level interface to the account management subsystem.
 * It acts as the primary entry point for application-level services (e.g., controllers)
 * to interact with the account domain. It orchestrates domain objects, repositories, and external
 * services to perform business operations like creating an account or exchanging currency.
 */
@RequiredArgsConstructor
public class AccountFacade {

    private final AccountRepository accountRepository;
    private final ExchangeRateClient exchangeRateClient;
    private final SupportedCurrencyRepository supportedCurrencyRepository;
    private final AccountFactory accountFactory;

    /**
     * Creates a new account based on the provided request.
     * <p>
     * This method uses an {@link AccountFactory} to construct a new {@link Account} aggregate.
     * It initializes the account with the user's first name, last name, and an initial balance in PLN.
     * The newly created account is then persisted to the repository.
     *
     * @param request The request object containing the details for the new account. Must not be null.
     * @return An {@link AccountView} representing the newly created account.
     */
    @Transactional
    public AccountView createAccount(@NonNull CreateAccountRequest request) {
        Account account = accountFactory.create(request.getFirstName(), request.getLastName(), request.getInitialBalancePLN());

        Account savedAccount = accountRepository.save(account);

        return savedAccount.toDto();
    }

    /**
     * Executes a currency exchange for a specified account.
     * <p>
     * This method orchestrates the entire exchange process. It retrieves the account,
     * validates that the source and target currencies are supported and different,
     * fetches the current exchange rate from an external provider, and then delegates
     * the core exchange logic to the Account aggregate. Finally, it persists the
     * updated state of the account.
     *
     * @param accountId The unique identifier for the account. Must not be null.
     * @param request   The exchange request details, including from/to currencies and amount. Must not be null.
     * @return An {@link AccountView} representing the updated state of the account after the exchange.
     * @throws AccountNotFoundException if the account with the specified ID does not exist.
     * @throws InvalidCurrencyException if the currencies are invalid or not supported.
     */
    @Transactional
    public AccountView exchangeCurrency(@NotNull String accountId, @NonNull ExchangeRequest request) {

        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account with ID " + accountId + " not found."));

        String fromCurrency = request.getFromCurrency().toUpperCase();
        String toCurrency = request.getToCurrency().toUpperCase();

        validateCurrencies(fromCurrency, toCurrency);

        BigDecimal rate = exchangeRateClient.getExchangeRate(fromCurrency, toCurrency);

        account.exchange(fromCurrency, toCurrency, request.getAmount(), rate);

        // Saving is handled by the transactional context, but an explicit save is more readable.
        Account savedAccount = accountRepository.save(account);

        return savedAccount.toDto();
    }

    private void validateCurrencies(String fromCurrency, String toCurrency) {
        validateCurrency(fromCurrency);
        validateCurrency(toCurrency);

        if (fromCurrency.equals(toCurrency)) {
            throw new InvalidCurrencyException("Source and target currency cannot be the same.");
        }
    }

    private void validateCurrency(String currencyCode) {
        if (!supportedCurrencyRepository.existsById(currencyCode)) {
            throw new InvalidCurrencyException("Currency " + currencyCode + " is not supported.");
        }
    }
}