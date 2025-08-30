package com.bank.model;

import com.bank.model.enums.Role;
import com.bank.utils.Constants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Nina Antic & Adriana Erakovic
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Access(AccessType.FIELD)
@Table(name = "customer", schema = Constants.DB_SCHEMA)
public class Customer extends AbstractEntity {

	@Id
	@NotNull
	@Column(name = "id", nullable = false, updatable = false)
	@GeneratedValue(generator = "CUSTOMER_ENTITY_ID_GENERATOR")
	@GenericGenerator(name = "CUSTOMER_ENTITY_ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
			@Parameter(name = "sequence_name", value = Constants.DB_SCHEMA + ".seq_customer"),
			@Parameter(name = "optimizer", value = "hilo"), @Parameter(name = "increment_size", value = "1") })
	private long id;

	@NotNull
	@Column(name = "first_name")
	private String firstName;
	
	@NotNull
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "birth_date", nullable = false, updatable = false)
	protected Date birthDate;

	@NotNull
	@Column(name = "last_name")
	private String lastName;

	@NotNull
	@Column(name = "user_name")
	private String userName;

	@NotNull
	@Column(name = "profile_picture")
	private String profilePicture;

	@NotNull
	@Column(name = "blocked")
	private Boolean blocked;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "role") // Admin ili User
	private Role role;

	@NotNull
	@Column(name = "email")
	private String email;

	@NotNull
	@Column(name = "password") // Hesirana 
	private String password;
}