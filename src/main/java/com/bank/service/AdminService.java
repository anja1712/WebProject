package com.bank.service;

import com.bank.model.Category;
import com.bank.model.Currency;
import com.bank.model.Transaction;
import com.bank.model.User;
import com.bank.model.dto.Dashboard;
import com.bank.model.enums.Role;
import com.bank.model.enums.UserStatus;
import com.bank.model.request.MonitoringRequest;
import com.bank.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
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
            throw new RuntimeException("User not found");
        }

        switch (targetStatus) {
            case ACTIVE:
                if (!user.getBlocked()) {
                    throw new RuntimeException("User is already active");
                }
                user.setBlocked(false);
                break;

            case BLOCKED:
                if (user.getBlocked()) {
                    throw new RuntimeException("User is already blocked");
                }
                user.setBlocked(true);
                break;

            default:
                throw new RuntimeException("Unsupported status: " + targetStatus);
        }

        userRepository.save(user);

        return "User " + id + " is now " + targetStatus.name();
    }

    public String addAdminNote(Long customerId, String note) {
        User user = userRepository.getUserById(customerId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        user.setAdminNote(note);
        userRepository.save(user);
        return "Note added for user " + customerId + ": " + note;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category addCategory(Category category) {
        Category existingCategory = categoryRepository.getCategoryByName(category.getName());
        if (existingCategory != null) {
            throw new RuntimeException("Predefined category with this name already exists");
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
            throw new RuntimeException("Category not found");
        }

        categoryRepository.delete(category);
        return "Category deleted";
    }

    public Currency addCurrency(Currency currency) {
        Currency existingCurrency = currencyRepository.getCurrencyByName(currency.getName());
        if (existingCurrency != null) {
            throw new RuntimeException("Currency with this name already exists");
        }

        currencyRepository.save(currency);
        return currency;
    }
    
    public Dashboard getDashboard() {
        BigInteger allUsers = BigInteger.valueOf(userRepository.count());
        BigInteger activeUsers = BigInteger.valueOf(userRepository.countByBlockedFalse());

        BigDecimal totalBalance = walletRepository.sumAllBalances();
        BigDecimal activeUserBalance = walletRepository.sumBalancesForActiveUsers();

        Instant oneMonthAgo = LocalDate.now()
                .minusMonths(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        Instant twoMinutesAgo = Instant.now().minus(2, ChronoUnit.MINUTES);

        List<Transaction> topTenMonthlyTransactions =
                transactionRepository.findTop10ByTransactionDateAfterOrderByAmountDesc(oneMonthAgo);

        List<Transaction> topTenTwoMinutesTransactions =
                transactionRepository.findTop10ByTransactionDateAfterOrderByAmountDesc(twoMinutesAgo);

        return Dashboard.builder()
                .allUsers(allUsers)
                .activeUsers(activeUsers)
                .activeUserBalance(activeUserBalance)
                .totalBalance(totalBalance)
                .topTenMonthlyTransactions(topTenMonthlyTransactions)
                .topTenTwoMinutesTransactions(topTenTwoMinutesTransactions)
                .build();
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