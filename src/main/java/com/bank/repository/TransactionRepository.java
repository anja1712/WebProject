package com.bank.repository;

import com.bank.model.Transaction;
import com.bank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> getTransactionByWalletId(@NotNull long walletId);
    List<Transaction> findByUser(User user);
	List<Transaction> findByRecurringTrue();
    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, Instant startDate, Instant endDate);

    List<Transaction> findTop10ByTransactionDateAfterOrderByAmountDesc(Instant fromDate);

}
