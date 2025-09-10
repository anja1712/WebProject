package com.bank.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.model.Currency;
import com.bank.model.Customer;
import com.bank.model.enums.CurrencyType;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {

	Currency getCurrencyByName(CurrencyType valueOf);

	Optional<Currency> findByName(String currencyCode);

}
