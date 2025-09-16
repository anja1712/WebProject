package com.bank.controller;

import com.bank.model.Transaction;
import com.bank.model.Wallet;
import com.bank.model.request.WalletRequest;
import com.bank.service.WalletService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.bank.model.enums.Role.ADMIN;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/create")
    public Wallet addWallet(@RequestBody WalletRequest walletRequest, HttpSession session) {
        Wallet wallet = walletService.addWallet(walletRequest);
        setAdminNote(wallet, session);
        return wallet;
    }

    @PutMapping("/{id}/update")
    public Wallet updateWallet(@PathVariable("id") Long id, @RequestBody WalletRequest walletRequest, HttpSession session) {
        Wallet wallet = walletService.updateWallet(id, walletRequest);
        setAdminNote(wallet, session);
        return wallet;
    }

    @GetMapping("/{id}")
    public Wallet getWallet(@PathVariable("id") Long id, HttpSession session) {
        Wallet wallet = walletService.getWallet(id, session);
        setAdminNote(wallet, session);
        return wallet;
    }

    @PutMapping("/{id}/archive")
    public void archiveWallet(@PathVariable("id") Long id, HttpSession session) { walletService.archiveWallet(id, session); }

    @DeleteMapping("/{id}")
    public void deleteWallet(@PathVariable("id") Long id, HttpSession session) { walletService.deleteWallet(id, session); }

    @GetMapping("/all")
    List<Wallet> getAllWallets(HttpSession session) {
        return walletService.getAllWallets(session);
    }

    @GetMapping("/{id}/transactions")
    public List<Transaction> getWalletTransactions(@PathVariable("id") Long id) {
        return walletService.getWalletTransactions(id);
    }

    private void setAdminNote(Wallet wallet, HttpSession session) {
        if (session.getAttribute("ROLE") != ADMIN) {
            wallet.getUser().setAdminNote(null);
        }
    }
}