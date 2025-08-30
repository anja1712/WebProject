package com.bank.repository;

import com.bank.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.validation.constraints.NotNull;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Customer getCustomerById(@NotNull long id);
}