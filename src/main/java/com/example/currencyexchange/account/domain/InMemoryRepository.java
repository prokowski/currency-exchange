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
 * An in-memory implementation of the {@link AccountRepository} for testing and development purposes.
 *
 * <p>This class simulates a database by storing {@link Account} aggregates in a thread-safe
 * {@link ConcurrentHashMap}. It serves as the "source of truth" for account data within the application's
 * lifecycle when a persistent database is not used. It correctly handles the {@link AccountId}
 * Value Object for lookups.</p>
 *
 * <p><b>Note:</b> Many methods inherited from JpaRepository are not supported as they
 * relate to database-specific features like transactions, flushing, or complex queries that
 * are not applicable to a simple in-memory map.</p>
 */
class InMemoryAccountRepository implements AccountRepository {

    /**
     * The underlying map that stores account data. The key is the raw string value of the AccountId.
     * This map is shared with {@link InMemoryAccountQueryRepository} to ensure data consistency.
     */
    final ConcurrentHashMap<String, Account> map = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Account> findByAccountId(AccountId accountId) {
        requireNonNull(accountId);
        return Optional.ofNullable(map.get(accountId.value()));
    }

    /**
     * {@inheritDoc}
     * <p>Saves or updates the given account in the in-memory map.</p>
     */
    @Override
    public <S extends Account> S save(S account) {
        requireNonNull(account);
        requireNonNull(account.getAccountId());
        map.put(account.getAccountId().value(), account);
        return account;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <S extends Account> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        entities.forEach(entity -> result.add(save(entity)));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Account> findAll() {
        return new ArrayList<>(map.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count() {
        return map.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Account entity) {
        requireNonNull(entity);
        map.remove(entity.getAccountId().value());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll(Iterable<? extends Account> entities) {
        entities.forEach(this::delete);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll() {
        map.clear();
    }

    // --- UNSUPPORTED JpaRepository METHODS ---

    @Override public void flush() { /* No-op in-memory */ }
    @Override public <S extends Account> S saveAndFlush(S entity) { return save(entity); }
    @Override public <S extends Account> List<S> saveAllAndFlush(Iterable<S> entities) { return saveAll(entities); }
    @Override public void deleteAllInBatch(Iterable<Account> entities) { deleteAll(entities); }
    @Override public void deleteAllInBatch() { deleteAll(); }
    @Override public Optional<Account> findById(Long aLong) { throw new UnsupportedOperationException("ID lookup is done via domain-specific AccountId."); }
    @Override public boolean existsById(Long aLong) { throw new UnsupportedOperationException("ID lookup is done via domain-specific AccountId."); }
    @Override public List<Account> findAllById(Iterable<Long> longs) { throw new UnsupportedOperationException("ID lookup is done via domain-specific AccountId."); }
    @Override public void deleteById(Long aLong) { throw new UnsupportedOperationException("ID lookup is done via domain-specific AccountId."); }
    @Override public void deleteAllById(Iterable<? extends Long> longs) { throw new UnsupportedOperationException("ID lookup is done via domain-specific AccountId."); }
    @Override public void deleteAllByIdInBatch(Iterable<Long> longs) { throw new UnsupportedOperationException("ID lookup is done via domain-specific AccountId."); }
    @Override @Deprecated public Account getOne(Long aLong) { throw new UnsupportedOperationException(); }
    @Override public Account getById(Long aLong) { throw new UnsupportedOperationException(); }
    @Override public Account getReferenceById(Long aLong) { throw new UnsupportedOperationException(); }
    @Override public List<Account> findAll(Sort sort) { throw new UnsupportedOperationException("Complex sorting not supported in-memory."); }
    @Override public Page<Account> findAll(Pageable pageable) { throw new UnsupportedOperationException("Pagination not supported in-memory."); }
    @Override public <S extends Account> Optional<S> findOne(Example<S> example) { throw new UnsupportedOperationException("Query by Example not supported in-memory."); }
    @Override public <S extends Account> List<S> findAll(Example<S> example) { throw new UnsupportedOperationException("Query by Example not supported in-memory."); }
    @Override public <S extends Account> List<S> findAll(Example<S> example, Sort sort) { throw new UnsupportedOperationException("Query by Example not supported in-memory."); }
    @Override public <S extends Account> Page<S> findAll(Example<S> example, Pageable pageable) { throw new UnsupportedOperationException("Query by Example not supported in-memory."); }
    @Override public <S extends Account> long count(Example<S> example) { throw new UnsupportedOperationException("Query by Example not supported in-memory."); }
    @Override public <S extends Account> boolean exists(Example<S> example) { throw new UnsupportedOperationException("Query by Example not supported in-memory."); }
    @Override public <S extends Account, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { throw new UnsupportedOperationException("Fluent Query not supported in-memory."); }

}

/**
 * An in-memory implementation of the {@link AccountQueryRepository} for the read-side of the CQRS pattern.
 *
 * <p>This class provides read-only access to account data, transforming domain entities into
 * flat DTOs ({@link AccountQuery}) suitable for presentation layers. It operates on the same
 * underlying map as {@link InMemoryAccountRepository} to ensure data consistency between the
 * write and read models.</p>
 */
@RequiredArgsConstructor
class InMemoryAccountQueryRepository implements AccountQueryRepository {

    private final ConcurrentHashMap<String, Account> map;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<AccountQuery> findByAccountId(@NonNull String accountId) {
        return Optional.ofNullable(map.get(accountId)).map(this::toQuery);
    }

    /**
     * Converts a domain {@link Account} entity into a read-model {@link AccountQuery} DTO.
     * <p>This method is responsible for mapping the rich domain model, including its Value Objects
     * (AccountId, PersonName, Money), into a simple, flat structure for querying.</p>
     *
     * @param account The domain entity to convert.
     * @return The corresponding {@link AccountQuery} DTO.
     */
    private AccountQuery toQuery(Account account) {
        if (account == null) return null;

        Set<AccountWalletQuery> walletQueries = account.getAccountWallets().stream()
                .map(wallet -> new AccountWalletQuery(
                        wallet.getEntityId(),
                        wallet.getBalance().currency().code(),
                        wallet.getBalance().amount()
                ))
                .collect(Collectors.toSet());

        return new AccountQuery(
                account.getEntityId(),
                account.getAccountId().value(),
                account.getPersonName().firstName(),
                account.getPersonName().lastName(),
                walletQueries
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AccountQuery> findAll() {
        return map.values().stream().map(this::toQuery).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override public long count() { return map.size(); }

    // --- UNSUPPORTED JpaRepository METHODS for Read-Only Repository ---

    @Override public <S extends AccountQuery> S save(S entity) { throw new UnsupportedOperationException("This is a read-only repository."); }
    @Override public <S extends AccountQuery> List<S> saveAll(Iterable<S> entities) { throw new UnsupportedOperationException("This is a read-only repository."); }
    @Override public Optional<AccountQuery> findById(Long aLong) { throw new UnsupportedOperationException("ID lookup is done via domain-specific AccountId."); }
    @Override public boolean existsById(Long aLong) { throw new UnsupportedOperationException("ID lookup is done via domain-specific AccountId."); }
    @Override public List<AccountQuery> findAllById(Iterable<Long> longs) { throw new UnsupportedOperationException("ID lookup is done via domain-specific AccountId."); }
    @Override public void deleteById(Long aLong) { throw new UnsupportedOperationException("This is a read-only repository."); }
    @Override public void delete(AccountQuery entity) { throw new UnsupportedOperationException("This is a read-only repository."); }
    @Override public void deleteAllById(Iterable<? extends Long> longs) { throw new UnsupportedOperationException("This is a read-only repository."); }
    @Override public void deleteAll(Iterable<? extends AccountQuery> entities) { throw new UnsupportedOperationException("This is a read-only repository."); }
    @Override public void deleteAll() { throw new UnsupportedOperationException("This is a read-only repository."); }
    @Override public void flush() { /* No-op in-memory */ }
    @Override public <S extends AccountQuery> S saveAndFlush(S entity) { throw new UnsupportedOperationException("This is a read-only repository."); }
    @Override public <S extends AccountQuery> List<S> saveAllAndFlush(Iterable<S> entities) { throw new UnsupportedOperationException("This is a read-only repository."); }
    @Override public void deleteAllInBatch(Iterable<AccountQuery> entities) { throw new UnsupportedOperationException("This is a read-only repository."); }
    @Override public void deleteAllByIdInBatch(Iterable<Long> longs) { throw new UnsupportedOperationException("This is a read-only repository."); }
    @Override public void deleteAllInBatch() { throw new UnsupportedOperationException("This is a read-only repository."); }
    @Override @Deprecated public AccountQuery getOne(Long aLong) { throw new UnsupportedOperationException(); }
    @Override public AccountQuery getById(Long aLong) { throw new UnsupportedOperationException(); }
    @Override public AccountQuery getReferenceById(Long aLong) { throw new UnsupportedOperationException(); }
    @Override public List<AccountQuery> findAll(Sort sort) { throw new UnsupportedOperationException("Complex sorting not supported in-memory."); }
    @Override public Page<AccountQuery> findAll(Pageable pageable) { throw new UnsupportedOperationException("Pagination not supported in-memory."); }
    @Override public <S extends AccountQuery> Optional<S> findOne(Example<S> example) { throw new UnsupportedOperationException("Query by Example not supported in-memory."); }
    @Override public <S extends AccountQuery> List<S> findAll(Example<S> example) { throw new UnsupportedOperationException("Query by Example not supported in-memory."); }
    @Override public <S extends AccountQuery> List<S> findAll(Example<S> example, Sort sort) { throw new UnsupportedOperationException("Query by Example not supported in-memory."); }
    @Override public <S extends AccountQuery> Page<S> findAll(Example<S> example, Pageable pageable) { throw new UnsupportedOperationException("Query by Example not supported in-memory."); }
    @Override public <S extends AccountQuery> long count(Example<S> example) { throw new UnsupportedOperationException("Query by Example not supported in-memory."); }
    @Override public <S extends AccountQuery> boolean exists(Example<S> example) { throw new UnsupportedOperationException("Query by Example not supported in-memory."); }
    @Override public <S extends AccountQuery, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { throw new UnsupportedOperationException("Fluent Query not supported in-memory."); }
}