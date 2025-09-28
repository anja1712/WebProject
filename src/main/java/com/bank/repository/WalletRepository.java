package com.bank.repository;

import com.bank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.model.Wallet;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Wallet getWalletById(@NotNull long id);

    List<Wallet> getWalletsByUserId(@NotNull Long userId);

    List<Wallet> user(User user);
}
