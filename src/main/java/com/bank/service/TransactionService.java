package com.bank.service;

import com.bank.model.Currency;
import com.bank.model.*;
import com.bank.model.dto.TransactionDTO;
import com.bank.model.dto.TransactionOverview;
import com.bank.model.enums.CurrencyType;
import com.bank.model.enums.Frequency;
import com.bank.model.enums.PeriodType;
import com.bank.model.enums.TransactionType;
import com.bank.model.request.TransactionRequest;
import com.bank.model.request.TransactionStatisticsRequest;
import com.bank.repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

import static com.bank.model.enums.Role.USER;

@Service
@RequiredArgsConstructor
public class TransactionService {
	private final TransactionRepository transactionRepository;
	private final WalletRepository walletRepository;
	private final UserRepository userRepository;
	private final CategoryRepository categoryRepository;
	private final CurrencyRepository currencyRepository;

	public Transaction makeTransaction(TransactionRequest request, HttpSession session) {
		Long userId = (Long) session.getAttribute("USER_ID");
		Transaction transactionToSave = new Transaction();

		User user = userRepository.getUserById(userId);
		if(user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id " + userId+ " not found");
		}

		Wallet wallet = walletRepository.getWalletById(request.getWalletId());
		if (wallet == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet with id " + request.getWalletId() + " not found");
		}

		if (!Objects.equals(wallet.getUser().getId(), userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User doesnt have a wallet with id " + request.getWalletId());
		}

		// if it's transfer, check if toWallet and fromWAllet exist and belong to user, set in transaction
		if (request.getSourceWalletId() != null && request.getDestinationWalletId() != null) {
			Wallet sourceWallet = walletRepository.getWalletById(request.getSourceWalletId());
			if (sourceWallet == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Source wallet with id " + request.getWalletId() + " not found");
			}
			if (!Objects.equals(sourceWallet.getUser().getId(), userId)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User doesnt have a wallet with id " + request.getSourceWalletId());
			}

			Wallet destinationWallet = walletRepository.getWalletById(request.getDestinationWalletId());
			if (destinationWallet == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination wallet with id " + request.getWalletId() + " not found");
			}
			if (!Objects.equals(destinationWallet.getUser().getId(), userId)) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User doesnt have a wallet with id " + request.getDestinationWalletId());
			}

			transactionToSave.setSourceWallet(sourceWallet);
			transactionToSave.setDestinationWallet(destinationWallet);
		}
		
		Currency currency = currencyRepository.getCurrencyByName(CurrencyType.valueOf(request.getCurrency()));
		if (currency == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction with currency " + request.getCurrency() + " is not allowed");
		}

		if (wallet.getCurrency().getId() != currency.getId()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wallet does not support " + currency.getName() + "transactions");
		}

		Category resultCategory = categoryRepository.findByNameAndTransactionTypeAndPredefinedTrue(request.getCategory(), request.getType());

		if (resultCategory == null) {
            resultCategory = categoryRepository.findByUserIdAndNameAndTransactionType(userId, request.getName(), request.getType());

			if (resultCategory == null) {
				resultCategory = Category.builder()
					.name(request.getName())
					.transactionType(request.getType())
					.creator(USER)
					.predefined(false)
					.userId(userId)
					.build();

				categoryRepository.save(resultCategory);
			}
		}

		BigDecimal resultAmount = null;
		if (wallet.getCurrentValue().compareTo(request.getAmount()) >= 0) {
			resultAmount = wallet.getCurrentValue().subtract(request.getAmount());
		} else if (wallet.getCurrentValue().compareTo(request.getAmount()) <= 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wallet doesn't have enough balance");
		}

		wallet.setCurrentValue(resultAmount);
		walletRepository.save(wallet);

		transactionToSave.setName(request.getName());
		transactionToSave.setCurrency(request.getCurrency());
		transactionToSave.setAmount(request.getAmount());
		transactionToSave.setTransactionType(request.getType());
		transactionToSave.setTransactionDate(request.getTransactionDate());
		transactionToSave.setCategory(resultCategory.getName());
		transactionToSave.setUser(user);
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
	
	public String transferFunds(Long fromWalletId, Long toWalletId, BigDecimal amount, HttpSession session, Currency transactionCurrency) {
		Object userId = session.getAttribute("USER_ID");

		Wallet fromWallet = walletRepository.getWalletById(fromWalletId);
		if(fromWallet == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Source wallet not found");
		}

		if (!userId.equals(fromWallet.getUser().getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Source wallet doesn't belong to logged in user");
		}

		Wallet toWallet = walletRepository.getWalletById(toWalletId);
		if (toWallet == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination wallet not found");
		}

		if (!userId.equals(toWallet.getUser().getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination wallet doesn't belong to logged in user");
		}
		
		
		
		Currency fromCurrency = fromWallet.getCurrency();
		Currency toCurrency = toWallet.getCurrency();
		
		BigDecimal firstConvertedAmount = amount;
		BigDecimal secondConvertedAmount = amount;
	
	//first conversion: transactionCurrency to fromCurrency
		if(!fromCurrency.equals(transactionCurrency)){
			BigDecimal fromRate = fromCurrency.getCurrencyValue();
			BigDecimal transactionRate = transactionCurrency.getCurrencyValue();
			
			BigDecimal amountInBase = amount.multiply(transactionRate);
			
			firstConvertedAmount = amountInBase.divide(fromRate, 2, RoundingMode.HALF_UP);
		}
		
		BigDecimal value = fromWallet.getCurrentValue();
		if(value.compareTo(firstConvertedAmount) < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
		}
		
	//second conversion: transactionCurrency to toCurrency	
		if(!transactionCurrency.equals(toCurrency)){
			BigDecimal transactionRate = transactionCurrency.getCurrencyValue();
			BigDecimal toRate = toCurrency.getCurrencyValue();
			BigDecimal amountInBase = amount.multiply(transactionRate);
			
			secondConvertedAmount = amountInBase.divide(toRate, 2, RoundingMode.HALF_UP);
		}
		
		fromWallet.setCurrentValue(fromWallet.getCurrentValue().subtract(firstConvertedAmount));
		toWallet.setCurrentValue(toWallet.getCurrentValue().add(secondConvertedAmount));
		
		walletRepository.save(fromWallet);
		walletRepository.save(toWallet);

		return secondConvertedAmount.setScale(2, RoundingMode.HALF_UP) + " transferred from " + fromWallet.getId() + " to " + toWallet.getId();
	}
		

	public List<TransactionDTO> getTransactions(Long userId, PeriodType periodType) {
		return userRepository.findById(userId)
				.map(transactionRepository::findByUser)
				.orElseGet(Collections::emptyList)
				.stream()
				.collect(Collectors.groupingBy(
						tx -> getPeriodStart(tx.getTransactionDate(), periodType),
						TreeMap::new,
						Collectors.toList()
				))
				.entrySet()
				.stream()
				.map(entry -> {
					BigDecimal total = entry.getValue().stream()
							.map(Transaction::getAmount)
							.reduce(BigDecimal.ZERO, BigDecimal::add);

					return new TransactionDTO(
							entry.getKey(),
							total,
							(long) entry.getValue().size()
					);
				})
				.toList();
	}


	public TransactionOverview getStatisticsForTimePeriod(TransactionStatisticsRequest request) {
		Long userId = request.getUserId();
		Instant start;
		Instant end;

		User user = userRepository.getUserById(userId);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer with id " + userId + " not found");
		}

		start = request.getStartDate() != null ?
				request.getStartDate() :
				user.getCreated();

		end = request.getEndDate() != null ?
				request.getEndDate() :
				Instant.now();

		List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, start, end);
		if (transactions.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No transactions found");
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

	private Instant getPeriodStart(Instant instant, PeriodType type) {
		ZoneId zone = ZoneId.systemDefault(); // or user-specific zone if needed
		LocalDateTime ldt = LocalDateTime.ofInstant(instant, zone);

		return switch (type) {
			case DAILY -> ldt.toLocalDate().atStartOfDay(zone).toInstant();
			case WEEKLY -> ldt
					.with(ChronoField.DAY_OF_WEEK, 1) // Monday as start, adjust if needed
					.toLocalDate()
					.atStartOfDay(zone)
					.toInstant();
			case MONTHLY -> ldt
					.withDayOfMonth(1)
					.toLocalDate()
					.atStartOfDay(zone)
					.toInstant();
			case QUARTERLY -> {
				int startMonth = ((ldt.getMonthValue() - 1) / 3) * 3 + 1;
				LocalDate firstOfQuarter = LocalDate.of(ldt.getYear(), startMonth, 1);
				yield firstOfQuarter.atStartOfDay(zone).toInstant();
			}
			case YEARLY -> LocalDate.of(ldt.getYear(), 1, 1)
					.atStartOfDay(zone)
					.toInstant();
		};
	}
	private Date calculateNextDate(Date lastProcessedDate, Frequency frequency) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(lastProcessedDate);
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

	
	public void disableRecurring(Long transactionId, HttpSession session) {
	    Optional<Transaction> optionalTransaction = transactionRepository.findById(transactionId);
	    if(optionalTransaction.isPresent()) {
	        Transaction t = optionalTransaction.get();

			if (!t.getUser().getId().equals(session.getAttribute("USER_ID"))) {
				throw new RuntimeException("Access denied");
			}

	        t.setRecurring(false);
	        t.setFrequency(null);
	        transactionRepository.save(t);
	    } else {
	        throw new RuntimeException("Transaction not found");
	    }
	}





//	public void processRecurringTransactions() {
//		List<Transaction> recurringTransactions = transactionRepository.findByRecurringTrue();
//
//		Date now = new Date();
//
//		for (Transaction t : recurringTransactions) {
//			Date nextDate = calculateNextDate(t.getTransactionDate(), t.getFrequency());
//			if (!nextDate.after(now)) {
//				Transaction newTransaction = new Transaction();
//				newTransaction.setName(t.getName());
//				newTransaction.setAmount(t.getAmount());
//				newTransaction.setTransactionType(t.getTransactionType());
//				newTransaction.setCategory(t.getCategory());
//				newTransaction.setUser(t.getUser());
//				newTransaction.setWallet(t.getWallet());
//				newTransaction.setRecurring(true);
//				newTransaction.setFrequency(t.getFrequency());
//				newTransaction.setTransactionDate(nextDate);
//
//				transactionRepository.save(newTransaction);
//
//				t.setTransactionDate(nextDate);
//				transactionRepository.save(t);
//			}
//		}
//	}
	public void processRecurringTransactions() {
		List<Transaction> recurringTransactions = transactionRepository.findByRecurringTrue();
		
		Date now = new Date();
		for (Transaction t : recurringTransactions) {
			Date lastProcessedDate = Date.from(t.getTransactionDate());              //we avoided using the type Instant
			Date nextDate = calculateNextDate(lastProcessedDate, t.getFrequency());
			
			List<Transaction> transactionsToSave = new ArrayList<>();
			
			while (!nextDate.after(now)) {
				Transaction newTransaction = new Transaction();
				
				newTransaction.setName(t.getName());
				newTransaction.setAmount(t.getAmount());
				newTransaction.setTransactionType(t.getTransactionType());
				newTransaction.setCategory(t.getCategory());
				newTransaction.setUser(t.getUser());
				newTransaction.setWallet(t.getWallet());
				newTransaction.setRecurring(true);
				newTransaction.setFrequency(t.getFrequency());
				newTransaction.setTransactionDate(nextDate.toInstant());
				
				updateWalletBalance(newTransaction);
				
				transactionsToSave.add(newTransaction);
				
				lastProcessedDate = nextDate;
				nextDate = calculateNextDate(lastProcessedDate, t.getFrequency());
			}
			
			if (!transactionsToSave.isEmpty()) {
				transactionRepository.saveAll(transactionsToSave);
				
				t.setTransactionDate(lastProcessedDate.toInstant());
				transactionRepository.save(t);
			}
		}
	}
	
	private void updateWalletBalance(Transaction transaction) {
		Wallet wallet = transaction.getWallet();
		BigDecimal currentValue = wallet.getCurrentValue();
		BigDecimal amount = transaction.getAmount();
		
		if (transaction.getTransactionType() == TransactionType.EXPENSE) {
			BigDecimal newValue = currentValue.subtract(amount);
			
			if (newValue.compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalStateException("Insufficient funds in the wallet: " + wallet.getName());
			}
			wallet.setCurrentValue(newValue);
		}
		walletRepository.save(wallet);
	}
	
	
}
