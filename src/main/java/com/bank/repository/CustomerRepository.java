package com.bank.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.model.Customer;
import com.bank.model.enums.Role;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

}