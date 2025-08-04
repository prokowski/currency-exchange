package com.example.currencyexchange.account

import com.example.currencyexchange.account.domain.dto.CreateAccountRequest
import com.example.currencyexchange.account.domain.dto.ExchangeRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions

import static org.hamcrest.Matchers.contains
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AccountControllerTestHelper {

    private static final String ACCOUNTS_API = "/api/accounts"
    private static final String ACCOUNT_DETAIL_API = "/api/accounts/{id}"
    private static final String ACCOUNT_EXCHANGE_API = "/api/accounts/{id}/exchange"

    // --- API Call Helper Methods ---

    static String createAccountViaApi(MockMvc mockMvc, ObjectMapper objectMapper, String firstName, String lastName, BigDecimal balance) {
        def request = new CreateAccountRequest(firstName: firstName, lastName: lastName, initialBalancePLN: balance)
        def response = performCreateAccount(mockMvc, objectMapper, request)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString()
        return objectMapper.readTree(response).get("accountId").asText()
    }

    static ResultActions performCreateAccount(MockMvc mockMvc, ObjectMapper objectMapper, CreateAccountRequest request) {
        return mockMvc.perform(post(ACCOUNTS_API)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
    }

    static ResultActions performGetAccount(MockMvc mockMvc, String accountId) {
        return mockMvc.perform(get(ACCOUNT_DETAIL_API, accountId))
    }

    static ResultActions performExchange(MockMvc mockMvc, ObjectMapper objectMapper, String accountId, ExchangeRequest request) {
        return mockMvc.perform(post(ACCOUNT_EXCHANGE_API, accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
    }

    // --- Assertion Helper Methods ---

    static void expectAccountCreated(ResultActions result, String expectedFirstName, double expectedPlnBalance) {
        result.andExpect(status().isCreated())
        result.andExpect(jsonPath('$.accountId').exists())
        result.andExpect(jsonPath('$.firstName').value(expectedFirstName))
        result.andExpect(jsonPath('$.balances[?(@.currencyCode == "PLN")].balance').value(contains(expectedPlnBalance)))
    }

    static void expectAccountDetails(ResultActions result, String expectedFirstName, double expectedPlnBalance) {
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.firstName').value(expectedFirstName))
        result.andExpect(jsonPath('$.balances[?(@.currencyCode == "PLN")].balance').value(contains(expectedPlnBalance)))
    }

    static void expectExchangeResult(ResultActions result, double expectedPln, double expectedUsd) {
        result.andExpect(status().isOk())
        result.andExpect(jsonPath('$.balances[?(@.currencyCode == "PLN")].balance').value(contains(expectedPln)))
        result.andExpect(jsonPath('$.balances[?(@.currencyCode == "USD")].balance').value(contains(expectedUsd)))
    }
}