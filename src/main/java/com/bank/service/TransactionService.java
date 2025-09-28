package com.bank.service;

import com.bank.model.Currency;
import com.bank.model.*;
import com.bank.model.dto.TransactionDTO;
import com.bank.model.dto.TransactionFullDto;
import com.bank.model.dto.TransactionOverview;
import com.bank.model.enums.CurrencyType;
import com.bank.model.enums.Frequency;
import com.bank.model.enums.PeriodType;
import com.bank.model.enums.TransactionType;
import com.bank.model.request.TransactionRequest;
import com.bank.model.request.TransactionStatisticsRequest;
import com.bank.repository.*;
import com.bank.utils.DtoMapper;
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
	private final DtoMapper dtoMapper;

	@PostConstruct
	public void init() {
		processRecurringTransactions();
	}

	public TransactionFullDto makeTransaction(TransactionRequest request, HttpSession session) {
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
		BigDecimal amountInWalletCurrency = request.getAmount();
		if (wallet.getCurrency().getId() != currency.getId()) {
			BigDecimal fromRate = currency.getCurrencyValue();
			BigDecimal toRate = wallet.getCurrency().getCurrencyValue();
			amountInWalletCurrency = request.getAmount()
					.multiply(toRate)
					.divide(fromRate, 2, RoundingMode.HALF_UP);
		}

		Category resultCategory = categoryRepository.findByNameAndTransactionTypeAndPredefinedTrue(request.getCategory(), request.getType());

		if (resultCategory == null) {
			resultCategory = categoryRepository.findByUserIdAndNameAndTransactionType(userId, request.getName(), request.getType());

			if (resultCategory == null) {
				resultCategory = Category.builder()
						.name(request.getCategory())
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
		transactionToSave.setTransactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : Instant.now());
		transactionToSave.setCategory(resultCategory.getName());
		transactionToSave.setUser(user);

		Wallet updatedWallet = updateWalletBalance(amountInWalletCurrency, transactionToSave.getTransactionType(), wallet);
		transactionToSave.setWallet(updatedWallet);

		if (request.getRecurring() && request.getFrequency() != null) {
			transactionToSave.setRecurring(true);
			transactionToSave.setFrequency(request.getFrequency());
		} else {
			transactionToSave.setRecurring(false);
		}

		transactionRepository.save(transactionToSave);

		return dtoMapper.toDto(transactionToSave);
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

		if (fromWallet.getCurrentAmount().compareTo(amount) < 0) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds");
		}

		// currency conversion: from → to
		BigDecimal fromRate = fromWallet.getCurrency().getCurrencyValue();
		BigDecimal toRate = toWallet.getCurrency().getCurrencyValue();

		BigDecimal convertedAmount = amount
				.multiply(toRate)
				.divide(fromRate, 2, RoundingMode.HALF_UP);

		// update balances
		fromWallet.setCurrentAmount(fromWallet.getCurrentAmount().subtract(amount));
		toWallet.setCurrentAmount(toWallet.getCurrentAmount().add(convertedAmount));

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
		List<Transaction> expenseTransactions = new ArrayList<>();

		for (Transaction transaction : transactions) {
			BigDecimal amount = transaction.getAmount();
			if (amount == null) continue;

			Currency transactionCurrency = currencyRepository.getCurrencyByName(
					CurrencyType.valueOf(transaction.getCurrency())
			);

			BigDecimal fromRate = transactionCurrency.getCurrencyValue();
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

		List<Map.Entry<String, BigDecimal>> topTenExpenses = expenseTransactions.stream()
				.sorted((t1, t2) -> {
					Currency c1 = currencyRepository.getCurrencyByName(CurrencyType.valueOf(t1.getCurrency()));
					Currency c2 = currencyRepository.getCurrencyByName(CurrencyType.valueOf(t2.getCurrency()));

					BigDecimal a1 = t1.getAmount().multiply(targetCurrency.getCurrencyValue())
							.divide(c1.getCurrencyValue(), 2, RoundingMode.HALF_UP);
					BigDecimal a2 = t2.getAmount().multiply(targetCurrency.getCurrencyValue())
							.divide(c2.getCurrencyValue(), 2, RoundingMode.HALF_UP);

					return a2.compareTo(a1);
				})
				.limit(10)
				.map(t -> {
					Currency c = currencyRepository.getCurrencyByName(CurrencyType.valueOf(t.getCurrency()));
					BigDecimal converted = t.getAmount().multiply(targetCurrency.getCurrencyValue())
							.divide(c.getCurrencyValue(), 2, RoundingMode.HALF_UP);

					return Map.entry(t.getId() + " - " + t.getName(), converted);
				})
				.collect(Collectors.toList());

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

	private Wallet updateWalletBalance(BigDecimal convertedAmount, TransactionType type, Wallet wallet) {
		BigDecimal walletAmount = wallet.getCurrentAmount();
		BigDecimal newAmount;

		if (type.equals(EXPENSE)) {
			newAmount = walletAmount.subtract(convertedAmount);
			if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Insufficient funds in the wallet: " + wallet.getName());
			}
		} else if (type.equals(INCOME)) {
			newAmount = walletAmount.add(convertedAmount);
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported transaction type");
		}

		wallet.setCurrentAmount(newAmount);
		wallet.setModified(Instant.now());
		return walletRepository.save(wallet);
	}

	public void processRecurringTransactions() {
		List<Transaction> recurringTransactions = transactionRepository.findByRecurringTrue();

		Instant now = Instant.now();
		List<Transaction> transactionsToSave = new ArrayList<>();

		for (Transaction t : recurringTransactions) {
			if (t.getFrequency() != null) {
				Instant lastProcessed = t.getTransactionDate();
				Instant nextDate = calculateNextDate(lastProcessed, t.getFrequency());

				while (!nextDate.isAfter(now)) {
					Transaction newTransaction = new Transaction();
					newTransaction.setName(t.getName());
					newTransaction.setCurrency(t.getCurrency());
					newTransaction.setAmount(t.getAmount());
					newTransaction.setTransactionType(t.getTransactionType());
					newTransaction.setCategory(t.getCategory());
					newTransaction.setUser(t.getUser());
					newTransaction.setWallet(t.getWallet());
					newTransaction.setRecurring(true);
					newTransaction.setFrequency(t.getFrequency());
					newTransaction.setTransactionDate(nextDate);

					updateWalletBalance(
							newTransaction.getAmount(),
							newTransaction.getTransactionType(),
							t.getWallet());

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

}