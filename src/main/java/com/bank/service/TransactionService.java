package com.bank.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.bank.model.Category;
import com.bank.model.Currency;
import com.bank.model.Customer;
import com.bank.model.Transaction;
import com.bank.model.Wallet;
import com.bank.model.enums.Frequency;
import com.bank.model.enums.PeriodType;
import com.bank.request.TransactionRequest;
import com.bank.repository.CategoryRepository;
import com.bank.repository.CustomerRepository;
import com.bank.repository.TransactionRepository;
import com.bank.repository.WalletRepository;

import dto.TransactionDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {
	private final TransactionRepository transactionRepository;
	private final WalletRepository walletRepository;
	private final CustomerRepository customerRepository;
	private final CategoryRepository categoryRepository;
	
	public Transaction addTransaction(TransactionRequest request) {	
		Customer customer = customerRepository.getCustomerById(request.getCustomerId());
		if(customer == null) {
			throw new RuntimeException("Customer with id " + request.getCustomerId() + " not found");
		}
				
		Category category = categoryRepository.getCategoryById(request.getCategoryId());
		if(category == null) {
			throw new RuntimeException("Category with id " + request.getCategoryId() + " not found");
		}
		
		Transaction transactionToSave = new Transaction();
		transactionToSave.setAmount(request.getAmount());
		transactionToSave.setType(request.getType());
		transactionToSave.setTransactionDate(request.getTransactionDate());
		transactionToSave.setCategory(request.getCategory());
		transactionToSave.setCustomer(customer);
	    transactionToSave.setWallet(walletRepository.getWalletById(request.getFromWalletId()));
	    transactionToSave.setRecurring(request.getRecurring() != null ? request.getRecurring() : false);
	    transactionToSave.setFrequency(request.getFrequency());
		
		return transactionRepository.save(transactionToSave);	
	}
	
	public void transferFunds(Long fromWalletId, Long toWalletId, BigDecimal amount) {
		Wallet fromWallet = walletRepository.getWalletById(fromWalletId);
		if(fromWallet == null) {
			throw new RuntimeException("Source wallet not found");
		}

		Wallet toWallet = walletRepository.getWalletById(toWalletId);
		if(toWallet == null) {
			throw new RuntimeException("Destination wallet not found");
		}
		
		BigDecimal value = fromWallet.getCurrentValue();
		if(value.compareTo(amount) < 0) {
			throw new RuntimeException("Insufficient funds");
		}
		
		Currency fromCurrency = fromWallet.getCurrency();
		Currency toCurrency = toWallet.getCurrency();
		
		BigDecimal convertedAmount = amount;
		if(!fromCurrency.equals(toCurrency)){
			BigDecimal fromRate = fromCurrency.getCurrencyValue();
			BigDecimal toRate = toCurrency.getCurrencyValue();
			
			BigDecimal amountInBase = amount.multiply(fromRate);
			
			convertedAmount = amountInBase.divide(toRate, 2, RoundingMode.HALF_UP);
		}
		
		fromWallet.setCurrentValue(fromWallet.getCurrentValue().subtract(amount));
		toWallet.setCurrentValue(toWallet.getCurrentValue().add(convertedAmount));
		walletRepository.save(fromWallet);
		walletRepository.save(toWallet);
	}
	
	public List<TransactionDTO> getTransaction(Long customerId, PeriodType periodType) {
	    Optional<Customer> optionalCustomer = customerRepository.findById(customerId);

	    if (optionalCustomer.isEmpty()) {
	        return new ArrayList<>();
	    }

	    Customer customer = optionalCustomer.get();
	    List<Transaction> transactions = transactionRepository.findByCustomer(customer);

	    Map<Date, List<Transaction>> groupedTransactions = new TreeMap<>();

	    for (Transaction transaction : transactions) {
	        Date periodStart = getPeriodStart(transaction.getTransactionDate(), periodType);
	        groupedTransactions
	            .computeIfAbsent(periodStart, k -> new ArrayList<>())
	            .add(transaction);
	    }

	    List<TransactionDTO> summaryList = new ArrayList<>();

	    for (Map.Entry<Date, List<Transaction>> entry : groupedTransactions.entrySet()) {
	        BigDecimal total = entry.getValue().stream()
	            .map(Transaction::getAmount)
	            .reduce(BigDecimal.ZERO, BigDecimal::add);

	        summaryList.add(new TransactionDTO(
	            entry.getKey(),
	            total,
	            (long) entry.getValue().size()
	        ));
	    }

	    return summaryList;
	}
	
	private Date getPeriodStart(Date date, PeriodType periodType) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);

	    switch (periodType) {
	        case DAILY:
	            cal.set(Calendar.HOUR_OF_DAY, 0);
	            cal.set(Calendar.MINUTE, 0);
	            cal.set(Calendar.SECOND, 0);
	            cal.set(Calendar.MILLISECOND, 0);
	            break;
	        case WEEKLY:
	            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
	            cal.set(Calendar.HOUR_OF_DAY, 0);
	            cal.set(Calendar.MINUTE, 0);
	            cal.set(Calendar.SECOND, 0);
	            cal.set(Calendar.MILLISECOND, 0);
	            break;
	        case MONTHLY:
	            cal.set(Calendar.DAY_OF_MONTH, 1);
	            cal.set(Calendar.HOUR_OF_DAY, 0);
	            cal.set(Calendar.MINUTE, 0);
	            cal.set(Calendar.SECOND, 0);
	            cal.set(Calendar.MILLISECOND, 0);
	            break;
	        case QUARTERLY:
	            int currentMonth = cal.get(Calendar.MONTH);
	            int startMonth = (currentMonth / 3) * 3; // kvartali: 0-2, 3-5, 6-8, 9-11
	            cal.set(Calendar.MONTH, startMonth);
	            cal.set(Calendar.DAY_OF_MONTH, 1);
	            cal.set(Calendar.HOUR_OF_DAY, 0);
	            cal.set(Calendar.MINUTE, 0);
	            cal.set(Calendar.SECOND, 0);
	            cal.set(Calendar.MILLISECOND, 0);
	            break;
	    }

	    return cal.getTime();
	}

	public void processRecurringTransactions() {
	    List<Transaction> recurringTransactions = transactionRepository.findByRecurringTrue();

	    Date now = new Date();

	    for (Transaction t : recurringTransactions) {
	    	//Ako je datum sledeće transakcije prošao (ili je danas)
	    	Date nextDate = calculateNextDate(t.getTransactionDate(), t.getFrequency());
	    	if (!nextDate.after(now)) {
	    		// Kreiraj novu transakciju sa sledećim datumom
	    		Transaction newTransaction = new Transaction();
	    		newTransaction.setName(t.getName());
	    		newTransaction.setAmount(t.getAmount());
	    		newTransaction.setType(t.getType());
	    		newTransaction.setCategory(t.getCategory());
	    		newTransaction.setCustomer(t.getCustomer());
	    		newTransaction.setWallet(t.getWallet());
	    		newTransaction.setRecurring(true);
	    		newTransaction.setFrequency(t.getFrequency());
	    		newTransaction.setTransactionDate(nextDate);

	    		transactionRepository.save(newTransaction);
	            
	    		t.setTransactionDate(nextDate);
	    		transactionRepository.save(t);
	        }
	    }
	}
	
	private Date calculateNextDate(Date date, Frequency frequency) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    switch (frequency) {
	        case WEEKLY:
	            cal.add(Calendar.WEEK_OF_YEAR, 1);
	            break;
	        case MONTHLY:
	            cal.add(Calendar.MONTH, 1);
	            break;
	        default:
	            throw new IllegalArgumentException("Unsupported frequency: " + frequency);
	    }
	    return cal.getTime();
	}

	
	public void disableRecurring(Long transactionId) {
	    Optional<Transaction> optionalTransaction = transactionRepository.findById(transactionId);
	    if(optionalTransaction.isPresent()) {
	        Transaction t = optionalTransaction.get();
	        t.setRecurring(false);
	        t.setFrequency(null);
	        transactionRepository.save(t);
	    } else {
	        throw new RuntimeException("Transaction not found");
	    }
	}
	
}
