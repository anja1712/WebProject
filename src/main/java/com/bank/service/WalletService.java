package com.bank.service;

import com.bank.model.*;
import com.bank.model.dto.TransactionViewDto;
import com.bank.model.enums.CurrencyType;
import com.bank.model.request.GoalRequest;
import com.bank.model.request.WalletRequest;
import com.bank.repository.*;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final TransactionRepository transactionRepository;
    private final GoalRepository goalRepository;

    public Wallet addWallet(WalletRequest request) {
        User customer = userRepository.getUserById(request.getUserId());
        if (customer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer with id " + request.getUserId() + " not found");
        }

        Currency currency = currencyRepository.getCurrencyByName(CurrencyType.valueOf(request.getCurrencyType().name()));
        if (currency == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Currency with name " + request.getCurrencyType() + " not found");
        }

        Wallet walletToSave = new Wallet();
        walletToSave.setName(request.getName());
        walletToSave.setInitValue(request.getInitialValue());
        walletToSave.setCurrentValue(request.getInitialValue());
        walletToSave.setCurrency(currency);
        walletToSave.setUser(customer);
        walletToSave.setSavings(request.getSavings());
        walletToSave.setArchive(false);

        return walletRepository.save(walletToSave);
    }

    @Transactional
    public Wallet updateWallet(Long id, WalletRequest request) {
        Wallet wallet = walletRepository.getWalletById(id);
        if(wallet == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet with id " + id + " not found");
        }

        Currency currency = currencyRepository.getCurrencyByName(CurrencyType.valueOf(request.getCurrencyType().name()));
        if (currency == null) {
            throw new RuntimeException("Currency with name " + request.getCurrencyType() + " not found");
        }

        if (request.getName() != null)
            wallet.setName(request.getName());

        if (request.getCurrentValue() != null)
            wallet.setCurrentValue(request.getCurrentValue());

        if (request.getSavings() != null)
            wallet.setSavings(request.getSavings());

        if(request.getCurrencyType() != null)
            wallet.setCurrency(currency);

        return walletRepository.save(wallet);
    }

    public List<Wallet> getAllWallets(HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        List<Wallet> wallets = walletRepository.getWalletsByUserId(userId) ;

        if (wallets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No wallets found");
        }

        return wallets;
    }

    public Wallet getWallet(Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        Wallet wallet = walletRepository.getWalletById(id);

        if (wallet == null || !Objects.equals(wallet.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet with id " + id + " not found");
        }

        return wallet;
    }

    public void archiveWallet(Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        Wallet wallet = walletRepository.getWalletById(id);
        if (wallet == null || !Objects.equals(wallet.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet with id " + id + " not found");
        }

        wallet.setArchive(true);
        walletRepository.save(wallet);
    }

    public void deleteWallet(Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        Wallet wallet = walletRepository.getWalletById(id);
        if (wallet == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet with id " + id + " not found");
        }
        if (!Objects.equals(wallet.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User doen't have wallet with id: " + id);
        }
        walletRepository.deleteById(id);
    }

    public List<TransactionViewDto> getWalletTransactions(Long walletId, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");

        Wallet wallet = walletRepository.getWalletById(walletId);
        if (wallet == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet not found");
        }

        if (!userId.equals(wallet.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Wallet doesn't belong to user");
        }

        return transactionRepository.findByWallet_Id(walletId).stream()
                .map(t -> TransactionViewDto.builder()
                        .name(t.getName())
                        .date(t.getTransactionDate())
                        .amount(t.getAmount())
                        .type(t.getTransactionType())
                        .currency(t.getCurrency())
                        .build()
                )
                .toList();
    }

    public Goal createGoal(GoalRequest goalRequest, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        Wallet wallet = walletRepository.getWalletById(goalRequest.getWalletId());
        if (wallet == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet with id " + goalRequest.getWalletId() + " not found");
        }

        if (!wallet.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User doesn't belong to user");
        }

        if (!wallet.getSavings()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Need to select savings wallet");
        }

        Goal goal = new Goal();
        goal.setName(goalRequest.getName());
        goal.setCurrentAmount(wallet.getCurrentValue());
        goal.setGoalAmount(goalRequest.getGoalAmount());
        goal.setWallet(wallet);
        goal.setUser(userRepository.findById(userId).get());
        goal.setStartDate(Instant.now());
        goal.setTargetDate(goalRequest.getTargetDate());

        goalRepository.save(goal);

        return goal;
    }
}
