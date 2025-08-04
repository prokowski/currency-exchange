package com.example.currencyexchange.account.domain;

import com.example.currencyexchange.account.query.AccountQuery;
import com.example.currencyexchange.account.query.AccountQueryRepository;
import com.example.currencyexchange.account.query.AccountWalletQuery;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * The main domain repository class that stores account data in memory.
 * It implements the AccountRepository interface and serves as the "source of truth" for the data.
 * It should be in the same package as the Account class to have access
 * to its package-private members.
 */
class InMemoryAccountRepository implements AccountRepository {

    /**
     * A shared map that stores data in memory. Package-private access
     * allows it to be shared with InMemoryAccountQueryRepository.
     */
    final ConcurrentHashMap<String, Account> map = new ConcurrentHashMap<>();

    @Override
    public Optional<Account> findByAccountId(String accountId) {
        return Optional.ofNullable(map.get(accountId));
    }

    @Override
    public <S extends Account> S save(S account) {
        requireNonNull(account);
        requireNonNull(account.getAccountId());
        map.put(account.getAccountId(), account);
        return account;
    }

    @Override
    public <S extends Account> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        entities.forEach(entity -> result.add(save(entity)));
        return result;
    }

    @Override
    public List<Account> findAll() {
        return new ArrayList<>(map.values());
    }

    @Override
    public long count() {
        return map.size();
    }

    @Override
    public void delete(Account entity) {
        requireNonNull(entity);
        map.remove(entity.getAccountId());
    }

    @Override
    public void deleteAll(Iterable<? extends Account> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAll() {
        map.clear();
    }

    @Override
    public void flush() {
        // In an in-memory implementation, there is no need to flush
    }

    @Override
    public <S extends Account> S saveAndFlush(S entity) {
        return save(entity);
    }

    @Override
    public <S extends Account> List<S> saveAllAndFlush(Iterable<S> entities) {
        List<S> savedEntities = saveAll(entities);
        flush();
        return savedEntities;
    }

    @Override
    public void deleteAllInBatch(Iterable<Account> entities) {
        deleteAll(entities);
    }

    @Override
    public void deleteAllInBatch() {
        deleteAll();
    }

    // --- Methods from JpaRepository that are not fully supported in the in-memory implementation ---

    @Override
    public Optional<Account> findById(Long aLong) {
        throw new UnsupportedOperationException("Operations based on Long ID are not supported. Use findByAccountId.");
    }

    @Override
    public boolean existsById(Long aLong) {
        throw new UnsupportedOperationException("Operations based on Long ID are not supported.");
    }

    @Override
    public List<Account> findAllById(Iterable<Long> longs) {
        throw new UnsupportedOperationException("Operations based on Long ID are not supported.");
    }

    @Override
    public void deleteById(Long aLong) {
        throw new UnsupportedOperationException("Operations based on Long ID are not supported.");
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
        throw new UnsupportedOperationException("Operations based on Long ID are not supported.");
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {
        throw new UnsupportedOperationException("Operations based on Long ID are not supported.");
    }

    @Override
    @Deprecated
    public Account getOne(Long aLong) {
        throw new UnsupportedOperationException("Operations based on Long ID are not supported.");
    }

    @Override
    public Account getById(Long aLong) {
        return findById(aLong).orElseThrow(() -> new RuntimeException("Account not found"));
    }

    @Override
    public Account getReferenceById(Long aLong) {
        throw new UnsupportedOperationException("Operations based on Long ID are not supported.");
    }

    @Override
    public List<Account> findAll(Sort sort) {
        throw new UnsupportedOperationException("Sorting is not implemented in InMemoryRepository.");
    }

    @Override
    public Page<Account> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Pagination is not implemented in InMemoryRepository.");
    }

    @Override
    public <S extends Account> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("QueryByExample is not implemented in InMemoryRepository.");
    }

    @Override
    public <S extends Account> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("QueryByExample is not implemented in InMemoryRepository.");
    }

    @Override
    public <S extends Account> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("QueryByExample is not implemented in InMemoryRepository.");
    }

    @Override
    public <S extends Account> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("QueryByExample is not implemented in InMemoryRepository.");
    }

    @Override
    public <S extends Account> long count(Example<S> example) {
        throw new UnsupportedOperationException("QueryByExample is not implemented in InMemoryRepository.");
    }

    @Override
    public <S extends Account> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("QueryByExample is not implemented in InMemoryRepository.");
    }

    @Override
    public <S extends Account, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("FluentQuery is not implemented in InMemoryRepository.");
    }
}

/**
 * Implementation of the "read" repository (query repository) for accounts.
 * It uses the same map as InMemoryAccountRepository to ensure data consistency.
 */
@RequiredArgsConstructor
class InMemoryAccountQueryRepository implements AccountQueryRepository {

    private final ConcurrentHashMap<String, Account> map;

    @Override
    public Optional<AccountQuery> findByAccountId(@NonNull String accountId) {
        return Optional.ofNullable(map.get(accountId)).map(this::toQuery);
    }

    private AccountQuery toQuery(Account account) {
        if (account == null) return null;

        Set<AccountWalletQuery> walletQueries = account.getAccountWallets().stream()
                .map(wallet -> new AccountWalletQuery(
                        wallet.getEntityId(),
                        wallet.getCurrencyCode(),
                        wallet.getBalance()))
                .collect(Collectors.toSet());

        return new AccountQuery(
                account.getEntityId(),
                account.getAccountId(),
                account.getFirstName(),
                account.getLastName(),
                walletQueries
        );
    }

    @Override
    public List<AccountQuery> findAll() {
        return map.values().stream().map(this::toQuery).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return map.size();
    }

    // --- Read-only or unsupported methods ---

    @Override
    public <S extends AccountQuery> S save(S entity) {
        throw new UnsupportedOperationException("Query repository is read-only.");
    }

    @Override
    public <S extends AccountQuery> List<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException("Query repository is read-only.");
    }

    @Override
    public Optional<AccountQuery> findById(Long aLong) {
        throw new UnsupportedOperationException("Use findByAccountId.");
    }

    @Override
    public boolean existsById(Long aLong) {
        throw new UnsupportedOperationException("Use findByAccountId.");
    }

    @Override
    public List<AccountQuery> findAllById(Iterable<Long> longs) {
        throw new UnsupportedOperationException("Use findByAccountId.");
    }

    @Override
    public void deleteById(Long aLong) {
        throw new UnsupportedOperationException("Query repository is read-only.");
    }

    @Override
    public void delete(AccountQuery entity) {
        throw new UnsupportedOperationException("Query repository is read-only.");
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
        throw new UnsupportedOperationException("Query repository is read-only.");
    }

    @Override
    public void deleteAll(Iterable<? extends AccountQuery> entities) {
        throw new UnsupportedOperationException("Query repository is read-only.");
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("Query repository is read-only.");
    }

    @Override
    public void flush() {
        // Not applicable
    }

    @Override
    public <S extends AccountQuery> S saveAndFlush(S entity) {
        throw new UnsupportedOperationException("Query repository is read-only.");
    }

    @Override
    public <S extends AccountQuery> List<S> saveAllAndFlush(Iterable<S> entities) {
        throw new UnsupportedOperationException("Query repository is read-only.");
    }

    @Override
    public void deleteAllInBatch(Iterable<AccountQuery> entities) {
        throw new UnsupportedOperationException("Query repository is read-only.");
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {
        throw new UnsupportedOperationException("Query repository is read-only.");
    }

    @Override
    public void deleteAllInBatch() {
        throw new UnsupportedOperationException("Query repository is read-only.");
    }

    @Override
    @Deprecated
    public AccountQuery getOne(Long aLong) {
        throw new UnsupportedOperationException("Use findByAccountId.");
    }

    @Override
    public AccountQuery getById(Long aLong) {
        return findById(aLong).orElseThrow(() -> new RuntimeException("AccountQuery not found"));
    }

    @Override
    public AccountQuery getReferenceById(Long aLong) {
        throw new UnsupportedOperationException("Use findByAccountId.");
    }

    @Override
    public List<AccountQuery> findAll(Sort sort) {
        throw new UnsupportedOperationException("Sorting is not implemented.");
    }

    @Override
    public Page<AccountQuery> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Pagination is not implemented.");
    }

    @Override
    public <S extends AccountQuery> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("QueryByExample is not implemented.");
    }

    @Override
    public <S extends AccountQuery> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("QueryByExample is not implemented.");
    }

    @Override
    public <S extends AccountQuery> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("QueryByExample is not implemented.");
    }

    @Override
    public <S extends AccountQuery> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("QueryByExample is not implemented.");
    }

    @Override
    public <S extends AccountQuery> long count(Example<S> example) {
        throw new UnsupportedOperationException("QueryByExample is not implemented.");
    }

    @Override
    public <S extends AccountQuery> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("QueryByExample is not implemented.");
    }

    @Override
    public <S extends AccountQuery, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("FluentQuery is not implemented.");
    }
}