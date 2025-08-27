package com.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.model.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

}
