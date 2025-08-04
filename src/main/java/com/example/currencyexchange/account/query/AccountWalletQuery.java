package com.example.currencyexchange.account.query;

import com.example.currencyexchange.account.domain.dto.AccountWalletView;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Getter
@Immutable
@Table(name = "account_wallet")
@AllArgsConstructor
@NoArgsConstructor
public class AccountWalletQuery {

    @Id
    private Long entityId;

    @NotNull
    @Size(max = 3)
    @Column(length = 3)
    private String currencyCode;

    @NotNull
    private BigDecimal balance;

    AccountWalletView toDto() {
        return AccountWalletView.builder()
                .currencyCode(currencyCode)
                .balance(balance)
                .build();
    }
}
