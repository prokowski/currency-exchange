package com.example.currencyexchange.account.domain.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Initial balance must be greater than zero")
    @DecimalMax(value = "9999999999.99", message = "Initial balance cannot exceed 10 billion.")
    @Digits(integer = 10, fraction = 2, message = "Invalid balance format. The required format is a maximum of 10 digits before the decimal point and 2 after.")
    private BigDecimal initialBalancePLN;
}
