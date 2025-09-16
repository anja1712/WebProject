package com.bank.service;

import com.bank.model.Currency;
import com.bank.model.Transaction;
import com.bank.model.User;
import com.bank.model.Wallet;
import com.bank.model.enums.CurrencyType;
import com.bank.model.request.WalletRequest;
import com.bank.repository.CurrencyRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.UserRepository;
import com.bank.repository.WalletRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public List<Transaction> getWalletTransactions(Long walletId) {
        Wallet wallet = walletRepository.getWalletById(walletId);
        if (wallet == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet with id " + walletId + " not found");
        }

        List<Transaction> transactions = transactionRepository.getTransactionByWalletId(wallet.getId());
        if (transactions == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transactions for wallet " + walletId + " not found");
        }
        return transactions;
    }
}
