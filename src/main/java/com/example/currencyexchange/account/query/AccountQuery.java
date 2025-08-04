package com.example.currencyexchange.account.query;

import com.example.currencyexchange.account.domain.dto.AccountView;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Immutable
@Table(name = "account")
@AllArgsConstructor
@NoArgsConstructor
public class AccountQuery {

    @Id
    private Long entityId;

    @NotNull
    @Column(unique = true)
    private String accountId;

    @NotNull
    @Size(max = 50)
    @Column(length = 50)
    private String firstName;

    @NotNull
    @Size(max = 50)
    @Column(length = 50)
    private String lastName;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_entity_id")
    private Set<AccountWalletQuery> wallets = new HashSet<>();

    public AccountView toDto() {
        return AccountView.builder()
                .accountId(accountId)
                .firstName(firstName)
                .lastName(lastName)
                .balances(wallets.stream()
                        .map(AccountWalletQuery::toDto).collect(Collectors.toList()))
                .build();
    }
}