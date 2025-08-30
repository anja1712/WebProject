package com.bank.model;

import com.bank.model.enums.CurrencyType;
import com.bank.utils.Constants;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@RequiredArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Access(AccessType.FIELD)
@Table(name = "currency", schema = Constants.DB_SCHEMA)
public class Currency extends AbstractEntity {

	@Id
	@NotNull
	@Column(name = "id", nullable = false, updatable = false)
	@GeneratedValue(generator = "CURRENCY_ENTITY_ID_GENERATOR")
	@GenericGenerator(name = "CURRENCY_ENTITY_ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
			@Parameter(name = "sequence_name", value = Constants.DB_SCHEMA + ".seq_currency"),
			@Parameter(name = "optimizer", value = "hilo"), @Parameter(name = "increment_size", value = "1") })
	private long id;
	
	@NotNull
	@Column(name = "name")
	@Enumerated(EnumType.STRING)
	private CurrencyType name;

	@NotNull
	@Column(name = "currency_value")
	private BigDecimal currencyValue;

	public Currency(CurrencyType currencyType, BigDecimal bigDecimal) {
		this.name = currencyType;
		this.currencyValue = bigDecimal;
	}
}
