package com.example.currencyexchange.shared.ddd;


import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.EqualsAndHashCode;

@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractAggregateEntity extends AbstractEntity {

	@Version
	private Long entityVersion = 0L;
}
