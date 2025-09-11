package com.bank.controller;

import com.bank.model.Transaction;
import com.bank.model.Wallet;
import com.bank.model.request.WalletRequest;
import com.bank.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/create")
    public Wallet addWallet(@RequestBody WalletRequest walletRequest) { return walletService.addWallet(walletRequest); }

    @PutMapping("/{id}/update")
    public Wallet addWallet(@PathVariable("id") Long id, @RequestBody WalletRequest walletRequest) {
        return walletService.updateWallet(id, walletRequest); }

    @GetMapping("/{id}")
    public Wallet getWallet(@PathVariable("id") Long id) { return walletService.getWallet(id); }

    @PutMapping("/{id}/archive")
    public void archiveWallet(@PathVariable("id") Long id) { walletService.archiveWallet(id); }

    @DeleteMapping("/{id}")
    public void deleteWallet(@PathVariable("id") Long id) { walletService.deleteWallet(id); }

    @GetMapping("/{id}/transactions")
    public List<Transaction> getWalletTransactions(@PathVariable("id") Long id) {
        return walletService.getWalletTransactions(id);
    }
}