package com.bank.model;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.bank.utils.Constants;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Access(AccessType.FIELD)
@Table(name = "wallet", schema = Constants.DB_SCHEMA)
public class Wallet extends AbstractEntity {

	@Id
	@NotNull
	@Column(name = "id", nullable = false, updatable = false)
	@GeneratedValue(generator = "Wallet_ENTITY_ID_GENERATOR")
	@GenericGenerator(name = "Wallet_ENTITY_ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
			@Parameter(name = "sequence_name", value = Constants.DB_SCHEMA + ".seq_wallet"),
			@Parameter(name = "optimizer", value = "hilo"), @Parameter(name = "increment_size", value = "1") })
	private long id;

	@NotNull
	@Column(name = "name")
	private String name;

	@NotNull
	@Column(name = "init_value")
	private BigDecimal initValue;

	@NotNull
	@Column(name = "current_value")
	private BigDecimal currentValue;
	
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "currency_id", nullable = false, updatable = true)
	private Currency currency;


	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "customer_id", nullable = false, updatable = true)
	private Customer customer;

	@NotNull
	@Column(name = "accumulation")
	private Boolean accumulation;
	
	@NotNull
	@Column(name = "savings")
	private Boolean savings;

	@NotNull
	@Column(name = "archive")
	private Boolean archive;
	
	@NotNull
	@Column(name = "goal_amount")
	private BigDecimal goalAmount;
	
	@NotNull
	@Column(name = "goal_deadline")
	private BigDecimal goalDeadline;
}
