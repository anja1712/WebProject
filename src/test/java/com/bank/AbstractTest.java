package com.bank;

import com.bank.model.User;
import com.bank.model.enums.Role;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

@ActiveProfiles("test")
public abstract class AbstractTest {

	protected User createUser() {

		User user = new User();
		user.setEmail("test@teest.com");
		user.setPassword("pass5678");
		user.setFirstName("Pera");
		user.setLastName("Peric");
		user.setBirthDate(Instant.now());
		user.setBlocked(true);
		user.setUserName("dxfcgvhbjn");
		user.setProfilePicture("aaaa");
		user.setRole(Role.ADMIN);

		return user;
	}
}
