package com.bank.model;

import com.bank.model.enums.Role;
import com.bank.utils.Constants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * @author Nina Antic & Adriana Erakovic
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Access(AccessType.FIELD)
@Table(name = "user", schema = Constants.DB_SCHEMA)
public class User extends AbstractEntity {

	@Id
	@NotNull
	@Column(name = "id", nullable = false, updatable = false)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_user")
	@SequenceGenerator(
			name = "seq_user",
			sequenceName = Constants.DB_SCHEMA + ".seq_user",
			allocationSize = 1
	)
	private Long id;

	@NotNull
	@Column(name = "first_name")
	private String firstName;
	
	@NotNull
	@Column(name = "birth_date", columnDefinition = "TIMESTAMPTZ")
	protected Instant birthDate;

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

	@Column(name = "admin_note") // Hesirana
	private String adminNote;
}