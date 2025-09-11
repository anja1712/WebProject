package com.bank.model;

import com.bank.model.enums.Frequency;
import com.bank.model.enums.TransactionType;
import com.bank.utils.Constants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Access(AccessType.FIELD)
@Table(name = "transaction", schema = Constants.DB_SCHEMA)
@Builder
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
	@Column(name = "currency")
	private String currency;

	@NotNull
	@Column(name = "amount")
	private BigDecimal amount;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "transaction_type") // Admin ili User
	private TransactionType transactionType;

	@NotNull
	@Column(name = "category")
	private String category;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "transaction_date", nullable = true, updatable = true)
	protected Date transactionDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modifitcation_date", nullable = true, updatable = true)
	protected Date modificationDate;

	@NotNull
	@Column(name = "recurring")
	private Boolean recurring;

	@Enumerated(EnumType.STRING)
	@Column(name = "frequency") // Monthly ili weekly
	private Frequency frequency;

	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "wallet_id", updatable = true)
	private Wallet wallet;

	@JsonIgnore
	@OneToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "customer_id", updatable = true)
	private Customer customer;

	@JsonIgnore
	//dodatna polja za transakcije izmedju 2 novcanika 
	@ManyToOne(fetch = FetchType.LAZY, optional = true)       //moze postojavi vise transakcija nad jednim novcanikom
	@JoinColumn(name = "source_wallet_id")
	private Wallet sourceWallet;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY, optional = true)
	@JoinColumn(name = "destination_wallet_id")
	private Wallet destinationWallet;
}