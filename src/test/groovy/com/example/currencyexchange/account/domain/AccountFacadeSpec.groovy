package com.example.currencyexchange.account.domain

import com.example.currencyexchange.account.domain.dto.CreateAccountRequest
import com.example.currencyexchange.account.domain.dto.ExchangeRequest
import com.example.currencyexchange.account.query.AccountQuery
import com.example.currencyexchange.infrastructure.nbp.ExchangeRateClient
import com.example.currencyexchange.account.exception.InsufficientFundsException
import com.example.currencyexchange.account.exception.InvalidCurrencyException
import spock.lang.Specification
import spock.lang.Unroll

class AccountFacadeSpec extends Specification {

    // Mocks for external dependencies
    private def exchangeRateClient = Mock(ExchangeRateClient)
    private def supportedCurrencyRepository = Mock(SupportedCurrencyRepository)

    // Setup test environment using the configuration factory
    private def accountConfiguration = new AccountConfiguration()
    private def testDependencies = accountConfiguration.createTestDependencies(exchangeRateClient, supportedCurrencyRepository)

    private def accountFacade = testDependencies.accountFacade
    private def accountQueryRepository = testDependencies.accountQueryRepository

    def cleanup() {
        // Clear the repository after each test to ensure isolation
        testDependencies.accountRepository.deleteAll()
    }

    def "should create account with initial PLN balance"() {
        given: "a request to create a new account"
        def request = new CreateAccountRequest("John", "Doe", new BigDecimal("1000.00"))

        when: "the account is created"
        def accountView = accountFacade.createAccount(request)

        then: "the account exists in the system with correct data"
        def accountQuery = accountQueryRepository.findByAccountId(accountView.getAccountId()).get()

        accountQuery.getFirstName() == "John"
        accountQuery.getLastName() == "Doe"
        accountQuery.getWallets().size() == 1

        and: "the wallet has the correct initial balance"
        def plnWallet = accountQuery.getWallets().find { it.getCurrencyCode() == "PLN" }
        plnWallet.getBalance() == new BigDecimal("1000.00")
    }

    @Unroll
    def "should throw exception when creating account with invalid data: #reason"() {
        given: "a request with invalid data"
        def request = new CreateAccountRequest(firstName, lastName, balance)

        when: "an attempt is made to create the account"
        accountFacade.createAccount(request)

        then: "an InvalidAccountDataException is thrown"
        thrown(InvalidAccountDataException)

        where:
        reason                      | firstName    | lastName   | balance
        "blank first name"          | ""           | "Kowalski" | new BigDecimal("100.00")
        "last name too long"        | "Jan"        | "a" * 51   | new BigDecimal("100.00")
        "zero initial balance"      | "Anna"       | "Nowak"    | BigDecimal.ZERO
        "negative initial balance"  | "Piotr"      | "Zieliński"| new BigDecimal("-10.00")
    }

    def "should exchange currencies successfully and return to initial balance"() {
        given: "an existing account with an initial PLN balance"
        def createRequest = new CreateAccountRequest("Jane", "Smith", new BigDecimal("1000.00"))
        def initialAccount = accountFacade.createAccount(createRequest)
        def accountId = initialAccount.getAccountId()

        and: "mocked currency support"
        supportedCurrencyRepository.existsById("PLN") >> true
        supportedCurrencyRepository.existsById("USD") >> true

        when: "an exchange from PLN to USD is requested"
        exchangeRateClient.getExchangeRate("PLN", "USD") >> new BigDecimal("0.25") // Interaction for the first call
        def plnToUsdRequest = new ExchangeRequest("PLN", "USD", new BigDecimal("400.00"))
        accountFacade.exchangeCurrency(accountId, plnToUsdRequest)

        then: "the account balances are updated correctly after the first exchange"
        AccountQuery accountAfterFirstExchange = accountQueryRepository.findByAccountId(accountId).get()
        accountAfterFirstExchange.getWallets().size() == 2
        accountAfterFirstExchange.getWallets().find { it.getCurrencyCode() == 'PLN' }.getBalance() == new BigDecimal("600.00")
        accountAfterFirstExchange.getWallets().find { it.getCurrencyCode() == 'USD' }.getBalance() == new BigDecimal("100.00")

        when: "the entire USD balance is exchanged back to PLN"
        exchangeRateClient.getExchangeRate("USD", "PLN") >> new BigDecimal("4.00") // Interaction for the second call
        def usdToPlnRequest = new ExchangeRequest("USD", "PLN", new BigDecimal("100.00")) // Exchange all USD back
        accountFacade.exchangeCurrency(accountId, usdToPlnRequest)

        then: "the account balances are updated and PLN balance is restored"
        AccountQuery finalAccount = accountQueryRepository.findByAccountId(accountId).get()
        finalAccount.getWallets().size() == 2
        finalAccount.getWallets().find { it.getCurrencyCode() == 'USD' }.getBalance() == new BigDecimal("0.00") // 100 - 100
        finalAccount.getWallets().find { it.getCurrencyCode() == 'PLN' }.getBalance() == new BigDecimal("1000.00") // 600 + (100 * 4.00)
    }

    def "should throw exception when exchanging with insufficient funds"() {
        given: "an existing account with limited PLN funds"
        def createRequest = new CreateAccountRequest("Peter", "Jones", new BigDecimal("100.00"))
        def initialAccount = accountFacade.createAccount(createRequest)
        def accountId = initialAccount.getAccountId()

        and: "mocked external services"
        supportedCurrencyRepository.existsById("PLN") >> true
        supportedCurrencyRepository.existsById("EUR") >> true
        exchangeRateClient.getExchangeRate("PLN", "EUR") >> new BigDecimal("0.22")

        when: "an attempt is made to exchange more money than available"
        def exchangeRequest = new ExchangeRequest("PLN", "EUR", new BigDecimal("150.00"))
        accountFacade.exchangeCurrency(accountId, exchangeRequest)

        then: "an InsufficientFundsException is thrown"
        thrown(InsufficientFundsException)
    }

    def "should throw exception for unsupported currency"() {
        given: "an existing account"
        def createRequest = new CreateAccountRequest("Mary", "Jane", new BigDecimal("1000.00"))
        def initialAccount = accountFacade.createAccount(createRequest)
        def accountId = initialAccount.getAccountId()

        and: "an unsupported currency 'XYZ'"
        supportedCurrencyRepository.existsById("PLN") >> true
        supportedCurrencyRepository.existsById("XYZ") >> false // Mark XYZ as unsupported

        when: "an exchange to the unsupported currency is requested"
        def exchangeRequest = new ExchangeRequest("PLN", "XYZ", new BigDecimal("100.00"))
        accountFacade.exchangeCurrency(accountId, exchangeRequest)

        then: "an InvalidCurrencyException is thrown"
        thrown(InvalidCurrencyException)
    }

    def "should throw exception when creating account with balance exceeding limit"() {
        given: "a request to create an account with a balance over the maximum limit"
        def request = new CreateAccountRequest("Midas", "King", new BigDecimal("10000000000.00"))

        when: "an attempt is made to create the account"
        accountFacade.createAccount(request)

        then: "a WalletBalanceLimitExceededException is thrown"
        thrown(WalletBalanceLimitExceededException)
    }

    def "should throw exception when exchange results in balance exceeding limit"() {
        given: "an account with a balance close to the maximum limit"
        def initialBalance = new BigDecimal("9999999999.00")
        def createRequest = new CreateAccountRequest("Richie", "Rich", initialBalance)
        def accountId = accountFacade.createAccount(createRequest).getAccountId()

        and: "mocked external services"
        supportedCurrencyRepository.existsById("PLN") >> true
        supportedCurrencyRepository.existsById("USD") >> true
        // Set a high rate to ensure the limit is exceeded
        exchangeRateClient.getExchangeRate("USD", "PLN") >> new BigDecimal("5.00")
        exchangeRateClient.getExchangeRate("PLN", "USD") >> new BigDecimal("0.25")

        // First, add some USD to the account to exchange from (100 PLN -> 25 USD)
        accountFacade.exchangeCurrency(accountId, new ExchangeRequest("PLN", "USD", new BigDecimal("100.00")))
        // Current PLN balance: 9999999899.00

        when: "an exchange is performed that would push the PLN balance over the limit"
        // We have 25 USD. Exchanging 25 USD at a rate of 5.0 gives 125 PLN.
        // 9999999899.00 + 125.00 = 10000000024.00, which is over the limit.
        def exchangeRequest = new ExchangeRequest("USD", "PLN", new BigDecimal("25.00"))
        accountFacade.exchangeCurrency(accountId, exchangeRequest)

        then: "a WalletBalanceLimitExceededException is thrown"
        thrown(WalletBalanceLimitExceededException)
    }

    def "should throw exception when exchanging to the same currency"() {
        given: "an existing account"
        def createRequest = new CreateAccountRequest("Sam", "Self", new BigDecimal("1000.00"))
        def accountId = accountFacade.createAccount(createRequest).getAccountId()

        and: "the currency is supported"
        supportedCurrencyRepository.existsById("PLN") >> true

        when: "an attempt is made to exchange from PLN to PLN"
        def exchangeRequest = new ExchangeRequest("PLN", "PLN", new BigDecimal("100.00"))
        accountFacade.exchangeCurrency(accountId, exchangeRequest)

        then: "an InvalidCurrencyException is thrown"
        thrown(InvalidCurrencyException)
    }

    def "should correctly deposit into an existing foreign currency wallet"() {
        given: "an account with an initial PLN balance"
        def createRequest = new CreateAccountRequest("Double", "Dip", new BigDecimal("1000.00"))
        def accountId = accountFacade.createAccount(createRequest).getAccountId()

        and: "mocked currency support"
        supportedCurrencyRepository.existsById("PLN") >> true
        supportedCurrencyRepository.existsById("USD") >> true

        and: "a first exchange from PLN to USD is performed"
        exchangeRateClient.getExchangeRate("PLN", "USD") >> new BigDecimal("0.25")
        def firstExchange = new ExchangeRequest("PLN", "USD", new BigDecimal("200.00")) // -> 50 USD
        accountFacade.exchangeCurrency(accountId, firstExchange)

        when: "a second exchange from PLN to USD is performed"
        def secondExchange = new ExchangeRequest("PLN", "USD", new BigDecimal("100.00")) // -> 25 USD
        accountFacade.exchangeCurrency(accountId, secondExchange)

        then: "the new amount is added to the existing USD wallet"
        AccountQuery finalAccount = accountQueryRepository.findByAccountId(accountId).get()
        finalAccount.getWallets().size() == 2 // No new wallet was created

        def usdWallet = finalAccount.getWallets().find { it.getCurrencyCode() == 'USD' }
        usdWallet.getBalance() == new BigDecimal("75.00") // 50 + 25
    }
}