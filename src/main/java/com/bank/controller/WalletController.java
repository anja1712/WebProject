package com.bank.controller;

import com.bank.model.Transaction;
import com.bank.model.Wallet;
import com.bank.model.request.WalletRequest;
import com.bank.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("wallet/create")
    public Wallet addWallet(@RequestBody WalletRequest walletRequest) { return walletService.addWallet(walletRequest); }

    @PutMapping("wallet/{id}/update")
    public Wallet addWallet(@PathVariable("id") Long id, @RequestBody WalletRequest walletRequest) {
        return walletService.updateWallet(id, walletRequest); }

    @GetMapping("wallet/{id}")
    public Wallet getWallet(@PathVariable("id") Long id) { return walletService.getWallet(id); }

    @PutMapping("wallet/{id}/archive")
    public void archiveWallet(@PathVariable("id") Long id) { walletService.archiveWallet(id); }

    @DeleteMapping("wallet/{id}")
    public void deleteWallet(@PathVariable("id") Long id) { walletService.deleteWallet(id); }

    @GetMapping("wallet/{id}/transactions")
    public List<Transaction> getWalletTransactions(@PathVariable("id") Long id) {
        return walletService.getWalletTransactions(id);
    }
}