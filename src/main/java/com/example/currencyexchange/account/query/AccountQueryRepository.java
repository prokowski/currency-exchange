package com.example.currencyexchange.account.query;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountQueryRepository extends JpaRepository<AccountQuery, Long> {
    Optional<AccountQuery> findByAccountId(String accountId);
}