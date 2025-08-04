package com.example.currencyexchange.account.domain;

import jakarta.persistence.Embeddable;
import lombok.NonNull;


/**
 * Represents a person's full name as a Value Object.
 *
 * <p>This class encapsulates the first and last name, providing a type-safe and
 * immutable way to handle personal names within the domain. As a Value Object,
 * its equality is based on its values (firstName and lastName), not on its reference.</p>
 *
 * <p>It enforces validation rules to ensure that both names are present and do not
 * exceed a maximum length.</p>
 */
@Embeddable
record PersonName(@NonNull String firstName, @NonNull String lastName) {

    /**
     * Compact constructor that validates the name components upon creation.
     *
     * @param firstName The person's first name.
     * @param lastName  The person's last name.
     * @throws InvalidAccountDataException if either name is blank or exceeds 50 characters.
     * @throws NullPointerException if either name is null.
     */
    PersonName {
        validateName(firstName, "First name");
        validateName(lastName, "Last name");
    }

    /**
     * A private helper method to validate a single name component.
     *
     * @param name      The name string to validate.
     * @param fieldName The name of the field being validated (e.g., "First name").
     */
    private static void validateName(String name, String fieldName) {
        if (name == null || name.isBlank()) {
            throw new InvalidAccountDataException(fieldName + " is required.");
        }
        if (name.length() > 50) {
            throw new InvalidAccountDataException(fieldName + " cannot exceed 50 characters.");
        }
    }
}