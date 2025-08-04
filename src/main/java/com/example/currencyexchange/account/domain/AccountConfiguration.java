package com.example.currencyexchange.account.domain;

import com.example.currencyexchange.account.query.AccountQueryRepository;
import com.example.currencyexchange.infrastructure.nbp.ExchangeRateClient;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AccountConfiguration {

    @Bean
    AccountFacade accountFacade(AccountRepository accountRepository, ExchangeRateClient exchangeRateClient, SupportedCurrencyRepository supportedCurrencyRepository) {
        AccountFactory accountFactory = new AccountFactory();
        return new AccountFacade(accountRepository, exchangeRateClient, supportedCurrencyRepository, accountFactory);
    }

    /**
     * A factory method for creating interconnected dependencies for testing purposes.
     * It is not marked as a @Bean, so it won't be processed by Spring.
     *
     * @param exchangeRateClient A dependency needed for the facade (can be a mock).
     * @param supportedCurrencyRepository A dependency needed for the facade (can be a mock).
     * @return A container with fully configured objects for tests.
     */
    public TestDependencies createTestDependencies(ExchangeRateClient exchangeRateClient, SupportedCurrencyRepository supportedCurrencyRepository) {
        InMemoryAccountRepository sharedRepository = new InMemoryAccountRepository();

        // The query repository uses the same map as the main repository to ensure data consistency.
        AccountQueryRepository queryRepository = new InMemoryAccountQueryRepository(sharedRepository.map);

        AccountFactory factory = new AccountFactory();
        AccountFacade facade = new AccountFacade(sharedRepository, exchangeRateClient, supportedCurrencyRepository, factory);

        return new TestDependencies(facade, queryRepository, sharedRepository);
    }

    /**
     * A static inner class acting as a container for test dependencies.
     * It makes it easier to return multiple interconnected objects from a single method.
     */
    @AllArgsConstructor
    public static class TestDependencies {
        public final AccountFacade accountFacade;
        public final AccountQueryRepository accountQueryRepository;
        public final InMemoryAccountRepository accountRepository; // Exposed for easier data preparation in tests
    }
}
