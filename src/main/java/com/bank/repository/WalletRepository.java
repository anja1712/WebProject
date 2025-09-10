package com.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.model.Wallet;

import javax.validation.constraints.NotNull;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Wallet getWalletById(@NotNull long id);
}
