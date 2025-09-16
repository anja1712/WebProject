package com.bank.repository;

import com.bank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.model.Wallet;
import org.springframework.data.jpa.repository.Query;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Wallet getWalletById(@NotNull long id);

    List<Wallet> getWalletsByUserId(@NotNull Long userId);

    List<Wallet> user(User user);

    @Query("SELECT COALESCE(SUM(w.currentValue), 0) FROM Wallet w")
    BigDecimal sumAllBalances();


    @Query("SELECT COALESCE(SUM(w.currentValue), 0) FROM Wallet w WHERE w.user.blocked = false")
    BigDecimal sumBalancesForActiveUsers();
}
