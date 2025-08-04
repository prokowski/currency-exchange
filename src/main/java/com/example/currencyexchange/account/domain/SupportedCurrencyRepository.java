package com.example.currencyexchange.account.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SupportedCurrencyRepository extends JpaRepository<SupportedCurrency, String> {
}