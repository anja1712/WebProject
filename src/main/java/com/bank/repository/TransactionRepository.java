package com.bank.repository;

import com.bank.model.Customer;
import com.bank.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> getTransactionByWalletId(@NotNull long walletId);
    List<Transaction> findByCustomer(Customer customer);
	List<Transaction> findByRecurringTrue();
    List<Transaction> findByCustomerIdAndTransactionDateBetween(Long customerId, Instant startDate, Instant endDate);
}
