package com.bank.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Version;
import lombok.Data;

import java.time.Instant;

/**
 * @author Nina Antic & Adriana Erakovic
 */
@MappedSuperclass
@Data
public abstract class AbstractEntity {

	@Column(name = "created", columnDefinition = "TIMESTAMPTZ", nullable = false, updatable = false)
	protected Instant created;

	@Column(name = "modified", columnDefinition = "TIMESTAMPTZ")
	protected Instant modified;

    @Version
	private Integer version;

	// za postavljanje vrednosti pre cuvanja u bazu, JPA anotacija
	@PrePersist
	public void prePersist() {
		if (created == null) {
			created = Instant.now();
		}
	}
}
