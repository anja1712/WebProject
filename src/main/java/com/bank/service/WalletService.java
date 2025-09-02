package com.bank.service;

import com.bank.model.Currency;
import com.bank.model.Customer;
import com.bank.model.Transaction;
import com.bank.model.Wallet;
import com.bank.model.enums.CurrencyType;
import com.bank.model.request.WalletRequest;
import com.bank.repository.CurrencyRepository;
import com.bank.repository.CustomerRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final CustomerRepository customerRepository;
    private final CurrencyRepository currencyRepository;
    private final TransactionRepository transactionRepository;

    public Wallet addWallet(WalletRequest request) {
        Customer customer = customerRepository.getCustomerById(request.getCustomerId());
        if (customer == null) {
            throw new RuntimeException("Customer with id " + request.getCustomerId() + " not found");
        }

        Currency currency = currencyRepository.getCurrencyByName(CurrencyType.valueOf(request.getCurrencyType().name()));
        if (currency == null) {
            throw new RuntimeException("Currency with name " + request.getCurrencyType() + " not found");
        }

        Wallet walletToSave = new Wallet();
        walletToSave.setName(request.getName());
        walletToSave.setInitValue(request.getInitialValue());
        walletToSave.setCurrentValue(request.getInitialValue());
        walletToSave.setCurrency(currency);
        walletToSave.setCustomer(customer);
        walletToSave.setSavings(request.getSavings());
        walletToSave.setArchive(false);

        return walletRepository.save(walletToSave);
    }

    @Transactional
    public Wallet updateWallet(Long id, WalletRequest request) {
        Wallet wallet = walletRepository.getWalletById(id);
        if(wallet == null) {
            throw new RuntimeException("Wallet with id " + id + " not found");
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

    public Wallet getWallet(Long id) {
        Wallet wallet = walletRepository.getWalletById(id);
        if (wallet == null) {
            throw new RuntimeException("Wallet with id " + id + " not found");
        }
        return wallet;
    }

    public void archiveWallet(Long id) {
        Wallet wallet = walletRepository.getWalletById(id);
        if (wallet == null) {
            throw new RuntimeException("Wallet with id " + id + " not found");
        }

        wallet.setArchive(true);
        walletRepository.save(wallet);
    }

    public void deleteWallet(Long id) {
        Wallet wallet = walletRepository.getWalletById(id);
        if (wallet == null) {
            throw new RuntimeException("Wallet with id " + id + " not found");
        }
        walletRepository.deleteById(id);
    }

    public List<Transaction> getWalletTransactions(Long walletId) {
        Wallet wallet = walletRepository.getWalletById(walletId);
        if (wallet == null) {
            throw new RuntimeException("Wallet with id " + walletId + " not found");
        }

        List<Transaction> transactions = transactionRepository.getTransactionByWalletId(wallet.getId());
        if (transactions == null) {
            throw new RuntimeException("Transactions for wallet " + walletId + " not found");
        }
        return transactions;
    }
}
