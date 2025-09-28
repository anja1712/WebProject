package com.bank.controller;

import com.bank.model.dto.GoalDto;
import com.bank.model.dto.TransactionViewDto;
import com.bank.model.dto.WalletDto;
import com.bank.model.request.GoalRequest;
import com.bank.model.request.WalletRequest;
import com.bank.service.WalletService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/create")
    public WalletDto addWallet(@RequestBody WalletRequest walletRequest) {
        return walletService.addWallet(walletRequest);
    }

    @PutMapping("/{id}/update")
    public WalletDto updateWallet(@PathVariable("id") Long id, @RequestBody WalletRequest walletRequest, HttpSession session) {
        return walletService.updateWallet(id, walletRequest, session);
    }

    @GetMapping("/{id}")
    public WalletDto getWallet(@PathVariable("id") Long id, HttpSession session) {
        return walletService.getWallet(id, session);
    }

    @PutMapping("/{id}/archive")
    public WalletDto archiveWallet(@PathVariable("id") Long id, HttpSession session) { return walletService.archiveWallet(id, session); }

    @PutMapping("/{id}/unarchive")
    public WalletDto unarchiveWallet(@PathVariable("id") Long id, HttpSession session) { return walletService.unarchiveWallet(id, session); }

    @DeleteMapping("/{id}")
    public void deleteWallet(@PathVariable("id") Long id, HttpSession session) { walletService.deleteWallet(id, session); }

    @GetMapping("/{userId}/all")
    List<WalletDto> getAllWallets(@PathVariable Long userId, HttpSession session) {
        Long loggedUserId = (Long) session.getAttribute("USER_ID");
        if (!Objects.equals(loggedUserId, userId)) {
            throw new ResponseStatusException(FORBIDDEN, "Target user is not logged in");
        }
        return walletService.getAllWallets(userId);
    }

    @GetMapping("/{id}/transactions")
    public List<TransactionViewDto> getWalletTransactions(@PathVariable("id") Long id, HttpSession session) {
        return walletService.getWalletTransactions(id, session);
    }

    @PostMapping("/goal")
    public GoalDto createGoal(@RequestBody GoalRequest goalRequest, HttpSession session) {
        return walletService.createGoal(goalRequest, session);
    }
}