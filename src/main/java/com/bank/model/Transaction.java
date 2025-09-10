package com.bank.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import com.bank.model.enums.AccountType;
import com.bank.model.enums.Frequency;
import com.bank.utils.Constants;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
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
@Table(name = "transaction", schema = Constants.DB_SCHEMA)
public class Transaction extends AbstractEntity {

	@Id
	@NotNull
	@Column(name = "id", nullable = false, updatable = false)
	@GeneratedValue(generator = "TRANSACTION_ENTITY_ID_GENERATOR")
	@GenericGenerator(name = "TRANSACTION_ENTITY_ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
			@Parameter(name = "sequence_name", value = Constants.DB_SCHEMA + ".seq_transaction"),
			@Parameter(name = "optimizer", value = "hilo"), @Parameter(name = "increment_size", value = "1") })
	private long id;

	@NotNull
	@Column(name = "name")
	private String name;

	@NotNull
	@Column(name = "amount")
	private BigDecimal amount;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "account_type") // Admin ili User
	private AccountType type;

	@OneToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "category_id", updatable = true)
	private Category category;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "transaction_date", nullable = true, updatable = true)
	protected Date transactionDate;

	@NotNull
	@Column(name = "recurring")
	private Boolean recurring;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "frequency") // Monthly ili weekly
	private Frequency frequency;

	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "wallet_id", updatable = true)
	private Wallet wallet;

	@OneToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "customer_id", updatable = true)
	private Customer customer;
	
	//dodatna polja za transakcije izmedju 2 novcanika 
	@ManyToOne(fetch = FetchType.LAZY, optional = true)       //moze postojavi vise transakcija nad jednim novcanikom
	@JoinColumn(name = "source_wallet_id")
	private Wallet sourceWallet;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "destination_wallet_id")
	private Wallet destinationWallet;

}