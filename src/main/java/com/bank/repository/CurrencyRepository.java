package com.bank.repository;

import com.bank.model.Currency;
import com.bank.model.enums.CurrencyType;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.validation.constraints.NotNull;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    Currency getCurrencyByName(@NotNull CurrencyType name);
}
