package com.bank.repository;

import com.bank.model.Customer;
import com.bank.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> getTransactionByWalletId(@NotNull long walletId);
    List<Transaction> findByCustomer(Customer customer);
	List<Transaction> findByRecurringTrue();
    List<Transaction> findByCustomerIdAndTransactionDateBetween(Long customerId, Instant startDate, Instant endDate);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t")
    BigDecimal sumAllBalances();


    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.customer.blocked = false")
    BigDecimal sumBalancesForActiveUsers();

    List<Transaction> findTop10ByTransactionDateAfterOrderByAmountDesc(Date fromDate);

}
