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
 * Provides a simplified, high-level interface to the account management subsystem.
 *
 * <p>This facade serves as the primary entry point for application-level services (e.g., controllers)
 * to interact with the account domain. It orchestrates domain objects, repositories, and external
 * services to execute business operations like creating an account or exchanging currency.
 * It is also responsible for translating data from DTOs into domain-specific Value Objects,
 * ensuring that the core domain logic operates on rich, validated objects.</p>
 */
@RequiredArgsConstructor
public class AccountFacade {

    private final AccountRepository accountRepository;
    private final ExchangeRateClient exchangeRateClient;
    private final SupportedCurrencyRepository supportedCurrencyRepository;
    private final AccountFactory accountFactory;

    /**
     * Creates a new account and persists it.
     *
     * <p>This method uses an {@link AccountFactory} to construct a new {@link Account} aggregate.
     * It initializes the account with the user's first name, last name, and an initial balance in PLN.
     * The newly created account is then saved to the repository.</p>
     *
     * @param request The request DTO containing the details for the new account. Must not be null.
     * @return An {@link AccountView} representing the newly created account.
     */
    @Transactional
    public AccountView createAccount(@NonNull CreateAccountRequest request) {
        Account account = accountFactory.create(request.getFirstName(), request.getLastName(), request.getInitialBalancePLN());
        Account savedAccount = accountRepository.save(account);
        return savedAccount.toDto();
    }

    /**
     * Executes a currency exchange operation for a specified account.
     *
     * <p>This method orchestrates the entire exchange process. It performs the following steps:
     * <ol>
     * <li>Translates raw input from the {@link ExchangeRequest} into rich domain Value Objects (AccountId, Money, Currency).</li>
     * <li>Validates that the source and target currencies are supported and are not the same.</li>
     * <li>Retrieves the {@link Account} aggregate from the repository.</li>
     * <li>Fetches the current exchange rate from an external provider.</li>
     * <li>Delegates the core exchange logic to the {@link Account} aggregate's business method.</li>
     * <li>Persists the updated state of the account.</li>
     * </ol>
     * </p>
     *
     * @param accountIdValue The unique identifier for the account as a String. Must not be null.
     * @param request        The DTO with exchange details (from/to currencies, amount). Must not be null.
     * @return An {@link AccountView} representing the updated state of the account after the exchange.
     * @throws AccountNotFoundException if no account with the specified ID exists.
     * @throws InvalidCurrencyException if any currency is invalid, unsupported, or if both currencies are the same.
     */
    @Transactional
    public AccountView exchangeCurrency(@NotNull String accountIdValue, @NonNull ExchangeRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Exchange amount must be positive.");
        }

        Account account = findAccountById(accountIdValue);

        Currency fromCurrency = new Currency(request.getFromCurrency());
        Currency toCurrency = new Currency(request.getToCurrency());
        Money amountToExchange = new Money(request.getAmount(), fromCurrency);

        validateCurrencies(fromCurrency, toCurrency);


        BigDecimal rateValue = exchangeRateClient.getExchangeRate(fromCurrency.code(), toCurrency.code());
        ExchangeRate rate = new ExchangeRate(fromCurrency, toCurrency, rateValue);

        account.exchange(amountToExchange, rate);

        Account savedAccount = accountRepository.save(account);
        return savedAccount.toDto();
    }

    private Account findAccountById(String accountId) {
        return accountRepository.findByAccountId(new AccountId(accountId))
                .orElseThrow(() -> new AccountNotFoundException("Account with id " + accountId + " not found."));
    }

    /**
     * Validates that both source and target currencies are supported and are different from each other.
     *
     * @param fromCurrency The source currency.
     * @param toCurrency   The target currency.
     * @throws InvalidCurrencyException if validation fails.
     */
    private void validateCurrencies(Currency fromCurrency, Currency toCurrency) {
        validateCurrencyIsSupported(fromCurrency);
        validateCurrencyIsSupported(toCurrency);

        if (fromCurrency.equals(toCurrency)) {
            throw new InvalidCurrencyException("Source and target currency cannot be the same.");
        }
    }

    /**
     * Checks if a given currency is supported by the system.
     *
     * @param currency The currency to validate.
     * @throws InvalidCurrencyException if the currency is not supported.
     */
    private void validateCurrencyIsSupported(Currency currency) {
        if (!supportedCurrencyRepository.existsById(currency.code())) {
            throw new InvalidCurrencyException("Currency " + currency.code() + " is not supported.");
        }
    }
}