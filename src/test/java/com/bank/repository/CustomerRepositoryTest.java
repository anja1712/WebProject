package com.bank.repository;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.bank.AbstractTest;
import com.bank.model.Currency;
import com.bank.model.Customer;
import com.bank.model.enums.CurrencyType;

@SpringBootTest
public class CustomerRepositoryTest extends AbstractTest {

	@Autowired
	private CustomerRepository repository;

	@Autowired
	private CurrencyRepository currencyRepository;

	@Test
	public void saveCustomerTest() {
		
	}
}
