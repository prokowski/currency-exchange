package com.example.currencyexchange.account

import com.example.currencyexchange.account.domain.dto.CreateAccountRequest
import com.example.currencyexchange.account.domain.dto.ExchangeRequest
import com.example.currencyexchange.infrastructure.nbp.ExchangeRateClient
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import spock.lang.Unroll

import static com.example.currencyexchange.account.AccountControllerTestHelper.*
import static org.mockito.Mockito.when
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerSpec extends Specification {

    @Autowired
    private MockMvc mockMvc

    @Autowired
    private ObjectMapper objectMapper

    @MockitoBean
    private ExchangeRateClient exchangeRateNbp

    def setup() {
        when(exchangeRateNbp.getExchangeRate("PLN", "USD")).thenReturn(new BigDecimal("0.25"))
        when(exchangeRateNbp.getExchangeRate("USD", "PLN")).thenReturn(new BigDecimal("4.00"))
    }

    def "should create account and return 201 Created"() {
        given: "a valid request to create an account"
        def request = new CreateAccountRequest(
                firstName: "Jan",
                lastName: "Kowalski",
                initialBalancePLN: new BigDecimal("1000.00")
        )

        when: "the create account endpoint is called"
        def result = performCreateAccount(mockMvc, objectMapper, request)

        then: "the response is successful and contains correct data"
        expectAccountCreated(result, "Jan", 1000.0)
    }

    @Unroll
    def "should return 400 Bad Request for invalid account creation data because #reason"() {
        given: "a request with invalid data"
        def invalidRequest = new CreateAccountRequest(
                firstName: firstName,
                lastName: lastName,
                initialBalancePLN: balance
        )

        when: "the create account endpoint is called"
        def result = performCreateAccount(mockMvc, objectMapper, invalidRequest)

        then: "the response status is 400 Bad Request"
        result.andExpect(status().isBadRequest())

        where:
        reason                                | firstName    | lastName   | balance
        "first name is blank"                 | ""           | "Kowalski" | new BigDecimal("100.00")
        "last name is blank"                  | "Jan"        | ""         | new BigDecimal("100.00")
        "last name is too long"               | "Jan"        | "b" * 51   | new BigDecimal("100.00")
        "first name is too long"              | "a" * 51     | "Nowak"    | new BigDecimal("100.00")
        "balance has too many decimal places" | "Anna"       | "Zielińska"| new BigDecimal("100.123")
        "balance is too large"                | "Midas"      | "Król"     | new BigDecimal("10000000000.00")
    }

    def "should get account by id and return 200 OK"() {
        given: "an existing account created via API"
        def accountId = createAccountViaApi(mockMvc, objectMapper, "Anna", "Nowak", new BigDecimal("500.00"))

        when: "the get account endpoint is called"
        def result = performGetAccount(mockMvc, accountId)

        then: "the response is successful and contains correct data"
        expectAccountDetails(result, "Anna", 500.0)
    }

    def "should return 404 Not Found when getting a non-existent account"() {
        given: "a non-existent account id"
        def accountId = "non-existent-id"

        when: "the get account endpoint is called"
        def result = performGetAccount(mockMvc, accountId)

        then: "the response status is 404 Not Found"
        result.andExpect(status().isNotFound())
    }

    def "should perform currency exchange and return 200 OK"() {
        given: "an existing account created via API"
        def accountId = createAccountViaApi(mockMvc, objectMapper, "Jan", "Kowalski", new BigDecimal("1000.00"))

        and: "a valid exchange request"
        def request = new ExchangeRequest(
                fromCurrency: "PLN",
                toCurrency: "USD",
                amount: new BigDecimal("200.00")
        )

        when: "the exchange endpoint is called"
        def result = performExchange(mockMvc, objectMapper, accountId, request)

        then: "the response is successful and contains updated balances"
        expectExchangeResult(result, 800.0, 50.0)
    }

    @Unroll
    def "should return 400 Bad Request for exchange with #reason"() {
        given: "an existing account"
        def accountId = createAccountViaApi(mockMvc, objectMapper, user, "User", initialPln)

        and: "the account state is prepared for the test"
        if (requiresInitialExchange) {
            def initialExchangeRequest = new ExchangeRequest(fromCurrency: "PLN", toCurrency: "USD", amount: new BigDecimal("400.00"))
            performExchange(mockMvc, objectMapper, accountId, initialExchangeRequest).andExpect(status().isOk())
        }

        and: "an exchange request for more money than available"
        def request = new ExchangeRequest(
                fromCurrency: fromCurrency,
                toCurrency: toCurrency,
                amount: amount
        )

        when: "the exchange endpoint is called"
        def result = performExchange(mockMvc, objectMapper, accountId, request)

        then: "the response status is 400 Bad Request"
        result.andExpect(status().isBadRequest())

        where:
        reason                  | user      | initialPln                | fromCurrency | toCurrency | amount                    | requiresInitialExchange
        "insufficient PLN funds"| "Biedny"  | new BigDecimal("100.00")  | "PLN"        | "USD"      | new BigDecimal("200.00")  | false
        "insufficient USD funds"| "Zamożny" | new BigDecimal("1000.00") | "USD"        | "PLN"      | new BigDecimal("200.00")  | true
    }
}