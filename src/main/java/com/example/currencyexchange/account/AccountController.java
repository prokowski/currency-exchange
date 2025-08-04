package com.example.currencyexchange.account;

import com.example.currencyexchange.account.domain.AccountFacade;
import com.example.currencyexchange.account.domain.dto.AccountView;
import com.example.currencyexchange.account.domain.dto.CreateAccountRequest;
import com.example.currencyexchange.account.domain.dto.ExchangeRequest;
import com.example.currencyexchange.account.exception.AccountNotFoundException;
import com.example.currencyexchange.account.query.AccountQuery;
import com.example.currencyexchange.account.query.AccountQueryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountFacade accountFacade;

    private final AccountQueryRepository accountQueryRepository;

    @PostMapping
    public ResponseEntity<AccountView> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountView accountView = accountFacade.createAccount(request);
        return new ResponseEntity<>(accountView, HttpStatus.CREATED);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountView> getAccount(@PathVariable String accountId) {
        AccountView accountView = accountQueryRepository.findByAccountId(accountId)
                .map(AccountQuery::toDto)
                .orElseThrow(() -> new AccountNotFoundException("Account with ID " + accountId + " not found."));
        return ResponseEntity.ok(accountView);
    }

    @PostMapping("/{accountId}/exchange")
    public ResponseEntity<AccountView> exchangeCurrency(
            @PathVariable String accountId,
            @Valid @RequestBody ExchangeRequest request) {
        AccountView updatedAccountView = accountFacade.exchangeCurrency(accountId, request);
        return ResponseEntity.ok(updatedAccountView);
    }
}