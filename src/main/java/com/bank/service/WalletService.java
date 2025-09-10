package com.bank.service;

import com.bank.model.Category;
import com.bank.model.Currency;
import com.bank.model.Customer;
import com.bank.model.Transaction;
import com.bank.model.Wallet;
import com.bank.model.enums.AccountType;
import com.bank.model.enums.CurrencyType;
import com.bank.request.TransactionRequest;
import com.bank.request.WalletRequest;
import com.bank.repository.CategoryRepository;
import com.bank.repository.CurrencyRepository;
import com.bank.repository.CustomerRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.WalletRepository;

import dto.TransactionRequestDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final CustomerRepository customerRepository;
    private final CurrencyRepository currencyRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    

    public Wallet addWallet(WalletRequest request) {
        Customer customer = customerRepository.getCustomerById(request.getCustomerId());
        if (customer == null) {
            throw new RuntimeException("Customer with id " + request.getCustomerId() + " not found");
        }

        Currency currency = currencyRepository.getCurrencyByName(CurrencyType.valueOf(request.getCurrency().toUpperCase()));
        if (currency == null) {
            throw new RuntimeException("Currency with name " + request.getCurrency() + " not found");
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

        Currency currency = currencyRepository.getCurrencyByName(CurrencyType.valueOf(request.getCurrency().toUpperCase()));
        if (currency == null) {
            throw new RuntimeException("Currency with name " + request.getCurrency() + " not found");
        }

        if (request.getName() != null)
            wallet.setName(request.getName());

        if (request.getCurrentValue() != null)
            wallet.setCurrentValue(request.getCurrentValue());

        if (request.getSavings() != null)
            wallet.setSavings(request.getSavings());

        if(request.getCurrency() != null)
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
    
    public Wallet createSavingsWallet(Long customerId, String name, BigDecimal initValue, String currencyCode, BigDecimal goalAmount, LocalDate goalDeadline) {
    	Customer customer = customerRepository.findById(customerId)
    		.orElseThrow(()-> new RuntimeException("Customer not found"));
    	
    	Currency currency = currencyRepository.findByName(currencyCode)
    		.orElseThrow(()-> new RuntimeException("Currency not found"));
    	
    	Wallet wallet = new Wallet();
    	wallet.setName(name);
    	wallet.setInitValue(initValue);
    	wallet.setCurrentValue(initValue);
    	wallet.setCurrency(currency);
    	wallet.setCustomer(customer);
    	wallet.setSavings(true);
    	wallet.setArchive(false);
    	
    	wallet.setGoalAmount(goalAmount);
    	wallet.setGoalDeadline(goalAmount);  
    	
    	return walletRepository.save(wallet);
    }
    
    @Transactional
    public Transaction createTransaction(TransactionRequestDTO request) {
    	Wallet wallet = walletRepository.findById(request.getWalletId())
    		.orElseThrow(()-> new RuntimeException("Wallet not found"));
    	
    	Category category = categoryRepository.findById(request.getCategoryId())
        	.orElseThrow(()-> new RuntimeException("Category not found"));
    	
    	Customer customer = customerRepository.findById(request.getCustomerId())
            .orElseThrow(()-> new RuntimeException("Category not found"));
    	
    	Transaction transaction = new Transaction();
    	transaction.setName(request.getName());
    	transaction.setAmount(request.getAmount());
    	transaction.setType(request.getType());
    	transaction.setCategory(category);
    	transaction.setTransactionDate(request.getTransactionDate());
    	transaction.setRecurring(request.getRecurring());
    	transaction.setFrequency(request.getFrequency());
    	transaction.setWallet(wallet);
    	transaction.setCustomer(customer);;
    	
    	//Azuriranje stanja novcanika na osnovu tipa transakcije
    	if(transaction.getType() == AccountType.INCOME){
    		wallet.setCurrentValue(wallet.getCurrentValue().add(transaction.getAmount()));;
    	}else if(transaction.getType() == AccountType.OUTCOME) {
    		wallet.setCurrentValue(wallet.getCurrentValue().subtract(transaction.getAmount()));
    	}
    	
    	walletRepository.save(wallet);
    	return transactionRepository.save(transaction);
    }
    
    
    
}
