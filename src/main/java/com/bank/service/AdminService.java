package com.bank.service;

import com.bank.model.Category;
import com.bank.model.Currency;
import com.bank.model.Transaction;
import com.bank.model.User;
import com.bank.model.dto.Dashboard;
import com.bank.model.dto.TransactionViewDto;
import com.bank.model.enums.CurrencyType;
import com.bank.model.enums.Role;
import com.bank.model.enums.UserStatus;
import com.bank.model.request.CurrencyRequest;
import com.bank.model.request.MonitoringRequest;
import com.bank.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CurrencyRepository currencyRepository;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    // block / unblock user
    public String changeCustomerStatus(Long id, UserStatus targetStatus) {
        User user = userRepository.getUserById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        switch (targetStatus) {
            case ACTIVE:
                if (!user.getBlocked()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already active");
                }
                user.setBlocked(false);
                break;

            case BLOCKED:
                if (user.getBlocked()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already blocked");
                }
                user.setBlocked(true);
                break;

            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported status: " + targetStatus);
        }

        userRepository.save(user);

        return "User " + id + " is now " + targetStatus.name();
    }

    public List<TransactionViewDto> getTransactionsForUser(Long userId) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        return transactionRepository.findByUserId(userId)
                .stream()
                .map(t -> TransactionViewDto.builder()
                        .name(t.getName())
                        .type(t.getTransactionType())
                        .amount(t.getAmount())
                        .date(t.getTransactionDate())
                        .currency(t.getCurrency())
                        .build())
                .toList();
    }

    public String addAdminNote(Long userId, String note) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        user.setAdminNote(note);
        userRepository.save(user);
        return "Note added for user " + userId + ": " + note;
    }

    public String deleteAdminNote(Long userId) {
        User user = userRepository.getUserById(userId);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        user.setAdminNote(null);
        userRepository.save(user);

        return "Note deleted for user " + userId + ": " + user.getAdminNote();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category addCategory(Category category) {
        Category existingCategory = categoryRepository.getCategoryByName(category.getName());
        if (existingCategory != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Predefined category with this name already exists");
        }

        category.setCreator(Role.ADMIN);
        category.setPredefined(true);
        categoryRepository.save(category);
        return category;
    }

    //TODO: delete category without deleting for existing records
    public String deleteCategory(Long id) {
        Category category = categoryRepository.getCategoryById(id);
        if (category == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }

        categoryRepository.delete(category);
        return "Category deleted";
    }

    public Currency addCurrency(CurrencyRequest request) {
        Currency currency;

        currency = currencyRepository.getCurrencyByName(CurrencyType.valueOf(request.getName()));
        if (currency != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Currency with this name already exists");
        }

        currency = new Currency();

        currency.setName(CurrencyType.valueOf(request.getName()));
        currency.setCurrencyValue(request.getExchangeRateToDollar());
        currency.setCreated(Instant.now());
        currency.setModified(Instant.now());

        currencyRepository.save(currency);
        return currency;
    }

    public List<Currency> getCurrencies() {
        return currencyRepository.findAll();
    }

    public void deleteCurrency(Long id) {
        currencyRepository.deleteById(id);
    }
    
    public Dashboard getDashboard() {
        Currency targetCurrency = currencyRepository.getCurrencyByName(CurrencyType.EUR);
        if (targetCurrency == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid target currency");
        }

        BigInteger allUsers = BigInteger.valueOf(userRepository.count());
        BigInteger activeUsers = BigInteger.valueOf(userRepository.countByBlockedFalse());

        BigDecimal totalBalance = walletRepository.findAll().stream()
                .map(wallet -> convertToTargetCurrency(wallet.getCurrentAmount(), wallet.getCurrency(), targetCurrency))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal activeUserBalance = walletRepository.findAll().stream()
                .filter(wallet -> !wallet.getUser().getBlocked())
                .map(wallet -> convertToTargetCurrency(wallet.getCurrentAmount(), wallet.getCurrency(), targetCurrency))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Instant oneMonthAgo = LocalDate.now()
                .minusMonths(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        Instant twoMinutesAgo = Instant.now().minus(2, ChronoUnit.MINUTES);

        Function<Transaction, BigDecimal> convertToTarget = t -> {
            BigDecimal fromRate = t.getWallet().getCurrency().getCurrencyValue();
            BigDecimal toRate = targetCurrency.getCurrencyValue();
            return t.getAmount().multiply(toRate).divide(fromRate, 2, RoundingMode.HALF_UP);
        };

        List<TransactionViewDto> topTenMonthlyTransactions =
                transactionRepository.findByTransactionDateAfter(oneMonthAgo).stream()
                        .sorted((t1, t2) -> convertToTarget.apply(t2).compareTo(convertToTarget.apply(t1)))
                        .limit(10)
                        .map(t -> TransactionViewDto.builder()
                                .name(t.getName())
                                .type(t.getTransactionType())
                                .date(t.getTransactionDate())
                                .amount(t.getAmount())
                                .currency(t.getCurrency())
                                .build())
                        .toList();

        List<TransactionViewDto> topTenTwoMinutesTransactions =
                transactionRepository.findByTransactionDateAfter(twoMinutesAgo).stream()
                        .sorted((t1, t2) -> convertToTarget.apply(t2).compareTo(convertToTarget.apply(t1)))
                        .limit(10)
                        .map(t -> TransactionViewDto.builder()
                                .name(t.getName())
                                .type(t.getTransactionType())
                                .date(t.getTransactionDate())
                                .amount(t.getAmount())
                                .currency(t.getCurrency())
                                .build())
                        .toList();

        return Dashboard.builder()
                .allUsers(allUsers)
                .activeUsers(activeUsers)
                .activeUserBalance(activeUserBalance)
                .totalBalance(totalBalance)
                .topTenMonthlyTransactions(topTenMonthlyTransactions)
                .topTenTwoMinutesTransactions(topTenTwoMinutesTransactions)
                .build();
    }

    private BigDecimal convertToTargetCurrency(BigDecimal amount, Currency fromCurrency, Currency targetCurrency) {
        if (fromCurrency.equals(targetCurrency)) return amount;
        BigDecimal fromRate = fromCurrency.getCurrencyValue();
        BigDecimal toRate = targetCurrency.getCurrencyValue();
        return amount.multiply(toRate).divide(fromRate, 2, RoundingMode.HALF_UP);
    }

    public List<Transaction> transactionMonitoring(MonitoringRequest request) {
        List<Transaction> transactions = transactionRepository.findAll();

        Stream<Transaction> stream = transactions.stream();

        if (request.getUserId()!= null) {
            stream = stream.filter(t -> t.getUser() != null && t.getUser().getId().equals(request.getUserId()));
        }

        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            stream = stream.filter(t -> request.getCategory().equalsIgnoreCase(t.getCategory()));
        }

        if (request.getAmount() != null) {
            if (request.isAmountGreaterOrEqual()) {
                stream = stream.filter(t -> t.getAmount().compareTo(request.getAmount()) >= 0);
            } else {
                stream = stream.filter(t -> t.getAmount().compareTo(request.getAmount()) <= 0);
            }
        }

        if (request.getDate() != null) {
            if (request.isDateAfterOrEqual()) {
                stream = stream.filter(t -> !t.getTransactionDate().isBefore(request.getDate())); // >=
            } else {
                stream = stream.filter(t -> !t.getTransactionDate().isAfter(request.getDate()));  // <=
            }
        }

        // sortiranje
        if (request.isSortByDate()) {
            stream = stream.sorted(Comparator.comparing(Transaction::getTransactionDate));
        } else if (request.isSortByAmount()) {
            stream = stream.sorted(Comparator.comparing(Transaction::getAmount));
        }

        return stream.toList();
    }

}