package com.bank.repository;

import com.bank.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CustomerRepositoryTest extends AbstractTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CurrencyRepository currencyRepository;

	@Test
	public void saveUserTest() {
		
	}
}
