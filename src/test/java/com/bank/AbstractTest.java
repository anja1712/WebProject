package com.bank;

import java.util.Date;

import org.springframework.test.context.ActiveProfiles;

import com.bank.model.Customer;
import com.bank.model.enums.Role;

@ActiveProfiles("test")
public abstract class AbstractTest {

	protected Customer createCustomer() {

		Customer customer = new Customer();
		customer.setEmail("test@teest.com");
		customer.setPassword("pass5678");
		customer.setFirstName("Pera");
		customer.setLastName("Peric");
		customer.setBirthDate(new Date());
		customer.setBlocked(true);
		customer.setUserName("dxfcgvhbjn");
		customer.setProfilePicture("aaaa");
		customer.setRole(Role.ADMIN);

		return customer;
	}
}
