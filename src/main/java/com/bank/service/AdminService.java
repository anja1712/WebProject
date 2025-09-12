package com.bank.service;

import com.bank.model.Category;
import com.bank.model.Currency;
import com.bank.model.Customer;
import com.bank.model.Transaction;
import com.bank.model.enums.CustomerStatus;
import com.bank.model.enums.Role;
import com.bank.repository.CategoryRepository;
import com.bank.repository.CurrencyRepository;
import com.bank.repository.CustomerRepository;
import com.bank.repository.TransactionRepository;

import dto.Dashboard;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final CurrencyRepository currencyRepository;
    private final TransactionRepository transactionRepository;
    

    //block / unblock user
    public String changeCustomerStatus(Long id, CustomerStatus targetStatus) {
        Customer customer = customerRepository.getCustomerById(id);
        if (customer == null) {
            throw new RuntimeException("User not found");
        }

        switch (targetStatus) {
            case ACTIVE:
                if (!customer.getBlocked()) {
                    throw new RuntimeException("User is already active");
                }
                customer.setBlocked(false);
                break;

            case BLOCKED:
                if (customer.getBlocked()) {
                    throw new RuntimeException("User is already blocked");
                }
                customer.setBlocked(true);
                break;

            default:
                throw new RuntimeException("Unsupported status: " + targetStatus);
        }

        customerRepository.save(customer);

        return "User " + id + " is now " + targetStatus.name();
    }


    public String addAdminNote(Long customerId, String note) {
        Customer customer = customerRepository.getCustomerById(customerId);
        if (customer == null) {
            throw new RuntimeException("User not found");
        }

        customer.setAdminNote(note);
        customerRepository.save(customer);
        return "Note added for user " + customerId;
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
    public String deleteCategory(String name) {
        Category category = categoryRepository.getCategoryByName(name);
        if (category == null) {
            throw new RuntimeException("Category not found");
        }

        categoryRepository.delete(category);
        return "Category deleted";
    }

    //this doesn't make much sense, currencies should be all predefined
    public Currency addCurrency(Currency currency) {
        Currency existingCurrency = currencyRepository.getCurrencyByName(currency.getName());
        if (existingCurrency != null) {
            throw new RuntimeException("Currency with this name already exists");
        }

        currencyRepository.save(currency);
        return currency;
    }
    
    public Dashboard getDashboard() {
        BigInteger allUsers = BigInteger.valueOf(customerRepository.count());
        BigInteger activeUsers = BigInteger.valueOf(customerRepository.countByBlockedFalse());

        BigDecimal totalBalance = transactionRepository.sumAllBalances();
        BigDecimal activeUserBalance = transactionRepository.sumBalancesForActiveUsers();

        Date oneMonthAgo = Date.from(
                LocalDate.now().minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        );
        Date twoMinutesAgo = Date.from(
                Instant.now().minus(2, ChronoUnit.MINUTES)
        );

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
}