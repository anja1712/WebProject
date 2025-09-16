package com.bank.model;

import com.bank.model.enums.Role;
import com.bank.model.enums.TransactionType;
import com.bank.utils.Constants;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.validation.constraints.NotNull;

@Entity
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Access(AccessType.FIELD)
@Table(name = "category", schema = Constants.DB_SCHEMA)
@AllArgsConstructor
@NoArgsConstructor
public class Category extends AbstractEntity {

	@Id
	@NotNull
	@Column(name = "id", nullable = false, updatable = false)
	@GeneratedValue(generator = "CATEGORY_ENTITY_ID_GENERATOR")
	@GenericGenerator(name = "CATEGORY_ENTITY_ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
			@Parameter(name = "sequence_name", value = Constants.DB_SCHEMA + ".seq_category"),
			@Parameter(name = "optimizer", value = "hilo"), @Parameter(name = "increment_size", value = "1") })
	private long id;

	@NotNull
	@Column(name = "name")
	private String name;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "role") // Admin ili User created this category
	private Role creator;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "transaction_type")
	private TransactionType transactionType;

	@NotNull
	@Column(name = "predefined")
	private Boolean predefined;

	@Column(name = "user_id")
	private Long userId;
}
