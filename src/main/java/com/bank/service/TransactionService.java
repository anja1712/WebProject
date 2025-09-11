package com.bank.service;

import com.bank.model.Currency;
import com.bank.model.*;
import com.bank.model.dto.TransactionOverview;
import com.bank.model.enums.CurrencyType;
import com.bank.model.enums.Frequency;
import com.bank.model.enums.PeriodType;
import com.bank.model.enums.TransactionType;
import com.bank.model.request.TransactionRequest;
import com.bank.model.request.TransactionStatisticsRequest;
import com.bank.repository.*;
import dto.TransactionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

import static com.bank.model.enums.Role.USER;

@Service
@RequiredArgsConstructor
public class TransactionService {
	private final TransactionRepository transactionRepository;
	private final WalletRepository walletRepository;
	private final CustomerRepository customerRepository;
	private final CategoryRepository categoryRepository;
	private final CurrencyRepository currencyRepository;

	public Transaction makeTransaction(TransactionRequest request) {
		Transaction transactionToSave = new Transaction();

		Customer customer = customerRepository.getCustomerById(request.getCustomerId());
		if(customer == null) {
			throw new RuntimeException("Customer with id " + request.getCustomerId() + " not found");
		}

		Wallet wallet = walletRepository.getWalletById(request.getWalletId());
		if (wallet == null) {
			throw new RuntimeException("Wallet with id " + request.getWalletId() + " not found");
		}

		if (request.getSourceWalletId() != null && request.getDestinationWalletId() != null) {
			Wallet sourceWallet = walletRepository.getWalletById(request.getSourceWalletId());
			if (sourceWallet == null) {
				throw new RuntimeException("Source wallet with id " + request.getWalletId() + " not found");
			}
			Wallet destinationWallet = walletRepository.getWalletById(request.getDestinationWalletId());
			if (destinationWallet == null) {
				throw new RuntimeException("Destination wallet with id " + request.getWalletId() + " not found");
			}
			transactionToSave.setSourceWallet(walletRepository.getWalletById(request.getSourceWalletId()));
			transactionToSave.setDestinationWallet(walletRepository.getWalletById(request.getDestinationWalletId()));
		}

		Currency currency = currencyRepository.getCurrencyByName(CurrencyType.valueOf(request.getCurrency()));
		if (currency == null) {
			throw new RuntimeException("Transaction with currency " + request.getCurrency() + " is not allowed");
		}

		if (wallet.getCurrency().getId() != currency.getId()) {
			throw new RuntimeException("Wallet does not support " + currency.getName() + "transactions");
		}
				
		Category category = categoryRepository.getCategoryByName(request.getCategory());
		if (category == null) {
			Category newCategory = Category.builder()
					.name(request.getName())
					.transactionType(request.getType())
					.creator(USER)
					.predefined(false)
					.customer(customer)
					.build();

			categoryRepository.save(newCategory);
		}

		transactionToSave.setName(request.getName());
		transactionToSave.setCurrency(request.getCurrency());
		transactionToSave.setAmount(request.getAmount());
		transactionToSave.setTransactionType(request.getType());
		transactionToSave.setTransactionDate(request.getTransactionDate());
		transactionToSave.setCategory(request.getCategory());
		transactionToSave.setCustomer(customer);
	    transactionToSave.setWallet(wallet);

		if (request.getRecurring() && request.getFrequency() != null) {
			transactionToSave.setRecurring(true);
			transactionToSave.setFrequency(request.getFrequency());
		} else {
			transactionToSave.setRecurring(false);
		}
		
		transactionRepository.save(transactionToSave);

		return transactionToSave;
	}
	
	public String transferFunds(Long fromWalletId, Long toWalletId, BigDecimal amount) {
		Wallet fromWallet = walletRepository.getWalletById(fromWalletId);
		if(fromWallet == null) {
			throw new RuntimeException("Source wallet not found");
		}

		Wallet toWallet = walletRepository.getWalletById(toWalletId);
		if (toWallet == null) {
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

		return convertedAmount + " transferred from " + fromWallet.getId() + " to " + toWallet.getId();
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

	public TransactionOverview getStatisticsForTimePeriod(TransactionStatisticsRequest request) {
		Long customerId = request.getCustomerId();
		Instant start;
		Instant end;

		Customer customer = customerRepository.getCustomerById(customerId);
		if (customer == null) {
			throw new RuntimeException("Customer with id " + customerId + " not found");
		}

		start = request.getStartDate() != null ?
				request.getStartDate().toInstant().minusMillis(1) :
				customer.getCreationDate().toInstant();

		end = request.getEndDate() != null ?
				request.getEndDate().toInstant().plusMillis(1) :
				Instant.now();

		List<Transaction> transactions = transactionRepository.findByCustomerIdAndTransactionDateBetween(customerId, start, end);
		if (transactions.isEmpty()) {
			throw new RuntimeException("No transactions found");
		}

		BigDecimal incomeSum = BigDecimal.valueOf(0.0);
		BigDecimal expenseSum = BigDecimal.valueOf(0.0);

		Map<String, BigDecimal> categoriesDisplay = new HashMap<>();// a map of categories and expenses for each
		Map<String, BigDecimal> topTenExpenses = new HashMap<>();
		List<Transaction> expenseTransactions = new ArrayList<>();

		for (Transaction transaction : transactions) {
			String categoryName = transaction.getCategory();

			if (transaction.getTransactionType() == TransactionType.INCOME) {
				if (transaction.getAmount()!= null)
					incomeSum = incomeSum.add(transaction.getAmount());
			}

			if (transaction.getTransactionType() == TransactionType.EXPENSE) {
				if (transaction.getAmount()!= null) {
					expenseSum = expenseSum.add(transaction.getAmount());
					expenseTransactions.add(transaction);
				}
			}

			if (categoriesDisplay.containsKey(categoryName)) {
				categoriesDisplay.put(categoryName, categoriesDisplay.get(categoryName).add(transaction.getAmount()));
			} else {
				categoriesDisplay.put(categoryName, transaction.getAmount());
			}
		}

		// sort expenses by amount desc and take top 10
		expenseTransactions.stream()
				.sorted((t1, t2) -> t2.getAmount().compareTo(t1.getAmount()))
				.limit(10)
				.forEach(t -> topTenExpenses.put(t.getName(), t.getAmount()));

		return TransactionOverview.builder()
				.income(incomeSum)
				.expense(expenseSum)
				.transactionsByCategories(categoriesDisplay)
				.topTenExpenses(topTenExpenses)
				.build();
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
	    	Date nextDate = calculateNextDate(t.getTransactionDate(), t.getFrequency());
	    	if (!nextDate.after(now)) {
	    		Transaction newTransaction = new Transaction();
	    		newTransaction.setName(t.getName());
	    		newTransaction.setAmount(t.getAmount());
	    		newTransaction.setTransactionType(t.getTransactionType());
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
