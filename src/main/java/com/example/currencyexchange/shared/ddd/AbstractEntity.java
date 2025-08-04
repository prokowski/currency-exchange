package com.example.currencyexchange.shared.ddd;

import jakarta.persistence.*;
import lombok.Getter;

@MappedSuperclass
public abstract class AbstractEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate_sequence")
	@SequenceGenerator(name = "hibernate_sequence", allocationSize = 1)

	@Getter
	protected Long entityId;

}
