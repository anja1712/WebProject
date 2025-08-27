package com.bank.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;
import lombok.Data;

/**
 * @author Nina Antic & Adriana Erakovic
 */
@MappedSuperclass
@Data
public abstract class AbstractEntity {

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creation_date", nullable = false, updatable = false)
	protected Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modification_date", nullable = true, updatable = true)
	protected Date modificationDate;

	@Version
	private Integer version;

	public Integer getVersion() {
		return version;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Date getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	// za postavljanje vrednosti pre cuvanja u bazu, JPA anotacija
	@PrePersist
	public void prePersist() {
		if (creationDate == null) {
			creationDate = new Date();
		}
	}

}
