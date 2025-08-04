package com.example.currencyexchange.infrastructure.nbp;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@RequiredArgsConstructor
class ExchangeRateNbp implements ExchangeRateClient {

    private static final String CURRENCY_CODE_PLN = "PLN";

    private final RestTemplate restTemplate;
    private final String apiUrlTemplate;

    @Override
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return BigDecimal.ONE;
        }

        // Get rate relative to PLN for both currencies
        BigDecimal fromRateInPln = getRateAgainstPln(fromCurrency);
        BigDecimal toRateInPln = getRateAgainstPln(toCurrency);

        // Calculate the final cross-rate
        return fromRateInPln.divide(toRateInPln, 4, RoundingMode.HALF_UP);
    }

    /**
     * Helper method to get the rate of a given currency against PLN.
     * Returns 1.0 if the currency is PLN itself.
     */
    private BigDecimal getRateAgainstPln(String currencyCode) {
        if (currencyCode.equalsIgnoreCase(CURRENCY_CODE_PLN)) {
            return BigDecimal.ONE;
        }
        return fetchRateForCurrency(currencyCode);
    }

    /**
     * Fetches the exchange rate for a single currency from the NBP API.
     */
    private BigDecimal fetchRateForCurrency(String currencyCode) {
        String url = apiUrlTemplate.replace("{currencyCode}", currencyCode);
        try {
            NbpResponse response = restTemplate.getForObject(url, NbpResponse.class);

            return Optional.of(response)
                    .map(NbpResponse::getRates)
                    .filter(rates -> !rates.isEmpty())
                    .map(rates -> rates.get(0).getMid())
                    .orElseThrow(() -> new IllegalStateException("Failed to retrieve rate for currency: " + currencyCode));

        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalStateException("Rate for currency " + currencyCode + " not found in NBP API.");
        } catch (Exception e) {
            throw new IllegalStateException("An error occurred while fetching rate for currency: " + currencyCode, e);
        }
    }
}
