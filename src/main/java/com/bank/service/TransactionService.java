package com.bank.service;

import com.bank.model.Currency;
import com.bank.model.*;
import com.bank.model.dto.TransactionDTO;
import com.bank.model.dto.TransactionOverview;
import com.bank.model.enums.CurrencyType;
import com.bank.model.enums.Frequency;
import com.bank.model.enums.PeriodType;
import com.bank.model.request.TransactionRequest;
import com.bank.model.request.TransactionStatisticsRequest;
import com.bank.repository.*;
import jakarta.annotation.PostConstruct;
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
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.bank.model.enums.Role.USER;
import static com.bank.model.enums.TransactionType.EXPENSE;
import static com.bank.model.enums.TransactionType.INCOME;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class TransactionService {
	private final TransactionRepository transactionRepository;
	private final WalletRepository walletRepository;
	private final UserRepository userRepository;
	private final CategoryRepository categoryRepository;
	private final CurrencyRepository currencyRepository;

	@PostConstruct
	public void init() {
		processRecurringTransactions();
	}

	public Transaction makeTransaction(TransactionRequest request, HttpSession session) {
		Long userId = (Long) session.getAttribute("USER_ID");
		Transaction transactionToSave = new Transaction();

		User user = userRepository.getUserById(userId);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id " + userId + " not found");
		}

		Wallet wallet = walletRepository.getWalletById(request.getWalletId());
		if (wallet == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet with id " + request.getWalletId() + " not found");
		}

		if (!Objects.equals(wallet.getUser().getId(), userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User doesnt have a wallet with id " + request.getWalletId());
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

		transactionToSave.setName(request.getName());
		transactionToSave.setCurrency(currency.getName().name());
		transactionToSave.setAmount(request.getAmount());
		transactionToSave.setTransactionType(request.getType());
		transactionToSave.setTransactionDate(request.getTransactionDate());
		transactionToSave.setCategory(resultCategory.getName());
		transactionToSave.setUser(user);

		Wallet updatedWallet = updateWalletBalance(request, wallet);
		transactionToSave.setWallet(updatedWallet);

		if (request.getRecurring() && request.getFrequency() != null) {
			transactionToSave.setRecurring(true);
			transactionToSave.setFrequency(request.getFrequency());
		} else {
			transactionToSave.setRecurring(false);
		}

		transactionRepository.save(transactionToSave);

		return transactionToSave;
	}

	public String transferFunds(Long fromWalletId, Long toWalletId, BigDecimal amount, HttpSession session) {
		Long userId = (Long) session.getAttribute("USER_ID");

		Wallet fromWallet = walletRepository.getWalletById(fromWalletId);
		Wallet toWallet = walletRepository.getWalletById(toWalletId);

		// existence + ownership checks
		if (fromWallet == null || !userId.equals(fromWallet.getUser().getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Source wallet invalid");
		}
		if (toWallet == null || !userId.equals(toWallet.getUser().getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination wallet invalid");
		}

		if (fromWallet.getCurrentValue().compareTo(amount) < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
		}

		// currency conversion: from → to
		BigDecimal fromRate = fromWallet.getCurrency().getCurrencyValue();
		BigDecimal toRate = toWallet.getCurrency().getCurrencyValue();

		BigDecimal convertedAmount = amount
				.multiply(toRate)
				.divide(fromRate, 2, RoundingMode.HALF_UP);

		// update balances
		fromWallet.setCurrentValue(fromWallet.getCurrentValue().subtract(amount));
		toWallet.setCurrentValue(toWallet.getCurrentValue().add(convertedAmount));

		walletRepository.save(fromWallet);
		walletRepository.save(toWallet);

		return convertedAmount + " " + toWallet.getCurrency().getName()
				+ " transferred from wallet " + fromWalletId + " to wallet " + toWalletId;
	}


	public List<TransactionDTO> getTransactions(Long userId, PeriodType periodType) {
		return userRepository.findById(userId)
				.map(transactionRepository::findByUser)
				.orElseGet(Collections::emptyList)
				.stream()
				.collect(Collectors.groupingBy(
						tx -> getPeriodStart(tx.getTransactionDate(), periodType),
						TreeMap::new,
						toList()
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
		Instant start = request.getStartDate() != null ? request.getStartDate() : userRepository.getUserById(userId).getCreated();
		Instant end = request.getEndDate() != null ? request.getEndDate() : Instant.now();

		User user = userRepository.getUserById(userId);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer with id " + userId + " not found");
		}

		List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId, start, end);
		if (transactions.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No transactions found");
		}


		Currency targetCurrency = currencyRepository.getCurrencyByName(CurrencyType.valueOf(request.getCurrency()));
		if (targetCurrency == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid target currency");
		}

		BigDecimal incomeSum = BigDecimal.ZERO;
		BigDecimal expenseSum = BigDecimal.ZERO;

		Map<String, BigDecimal> categoriesDisplay = new HashMap<>();
		Map<String, BigDecimal> topTenExpenses = new HashMap<>();
		List<Transaction> expenseTransactions = new ArrayList<>();

		for (Transaction transaction : transactions) {
			BigDecimal amount = transaction.getAmount();
			if (amount == null) continue;

			BigDecimal fromRate = transaction.getWallet().getCurrency().getCurrencyValue();
			BigDecimal toRate = targetCurrency.getCurrencyValue();
			BigDecimal convertedAmount = amount.multiply(toRate).divide(fromRate, 2, RoundingMode.HALF_UP);

			String categoryName = transaction.getCategory();

			if (transaction.getTransactionType() == INCOME) {
				incomeSum = incomeSum.add(convertedAmount);
			} else if (transaction.getTransactionType() == EXPENSE) {
				expenseSum = expenseSum.add(convertedAmount);
				expenseTransactions.add(transaction);
			}

			categoriesDisplay.merge(categoryName, convertedAmount, BigDecimal::add);
		}

		expenseTransactions.stream()
				.sorted((t1, t2) -> {
					BigDecimal a1 = t1.getAmount().multiply(targetCurrency.getCurrencyValue())
							.divide(t1.getWallet().getCurrency().getCurrencyValue(), 2, RoundingMode.HALF_UP);
					BigDecimal a2 = t2.getAmount().multiply(targetCurrency.getCurrencyValue())
							.divide(t2.getWallet().getCurrency().getCurrencyValue(), 2, RoundingMode.HALF_UP);
					return a2.compareTo(a1);
				})
				.limit(10)
				.forEach(t -> {
					BigDecimal converted = t.getAmount().multiply(targetCurrency.getCurrencyValue())
							.divide(t.getWallet().getCurrency().getCurrencyValue(), 2, RoundingMode.HALF_UP);
					topTenExpenses.put(t.getName(), converted);
				});

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

	private Instant calculateNextDate(Instant date, Frequency frequency) {
        return switch (frequency) {
            case DAILY -> date.plus(1, ChronoUnit.DAYS);
            case WEEKLY -> date.plus(1, ChronoUnit.WEEKS);
            case MONTHLY -> date.plus(1, ChronoUnit.MONTHS);
        };
	}


	public void disableRecurring(Long transactionId, HttpSession session) {
		Optional<Transaction> optionalTransaction = transactionRepository.findById(transactionId);
		if (optionalTransaction.isPresent()) {
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

	private Wallet updateWalletBalance(TransactionRequest request, Wallet wallet) {
		BigDecimal walletAmount = wallet.getCurrentValue();
		BigDecimal transactionAmount = request.getAmount();
		BigDecimal newAmount = BigDecimal.ZERO;

		if (request.getType().equals(EXPENSE)) {
			newAmount = walletAmount.subtract(transactionAmount);
			if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds in the wallet: " + wallet.getName());
			}

		} else if (request.getType().equals(INCOME)) {
			newAmount = walletAmount.add(transactionAmount);
		}

		wallet.setCurrentValue(newAmount);
		walletRepository.save(wallet);
		return wallet;
	}

	public void processRecurringTransactions() {
		List<Transaction> recurringTransactions = transactionRepository.findByRecurringTrue();

		Instant now = Instant.now();

		for (Transaction t : recurringTransactions) {
			Instant lastProcessed = t.getTransactionDate();
			Instant nextDate = calculateNextDate(lastProcessed, t.getFrequency());

			List<Transaction> transactionsToSave = new ArrayList<>();

			while (!nextDate.isAfter(now)) {
				Transaction newTransaction = new Transaction();
				newTransaction.setName(t.getName());
				newTransaction.setAmount(t.getAmount());
				newTransaction.setTransactionType(t.getTransactionType());
				newTransaction.setCategory(t.getCategory());
				newTransaction.setUser(t.getUser());
				newTransaction.setWallet(t.getWallet());
				newTransaction.setRecurring(true);
				newTransaction.setFrequency(t.getFrequency());
				newTransaction.setTransactionDate(nextDate);

				updateWalletBalance(TransactionRequest.builder()
								.type(newTransaction.getTransactionType())
								.amount(newTransaction.getAmount())
								.build(), t.getWallet());

				transactionsToSave.add(newTransaction);

				lastProcessed = nextDate;
				nextDate = calculateNextDate(lastProcessed, t.getFrequency());
			}

			if (!transactionsToSave.isEmpty()) {
				transactionRepository.saveAll(transactionsToSave);

				t.setTransactionDate(lastProcessed);
				transactionRepository.save(t);
			}
		}
	}

}