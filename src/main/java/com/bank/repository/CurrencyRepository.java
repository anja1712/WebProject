package com.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.model.Currency;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {

}
