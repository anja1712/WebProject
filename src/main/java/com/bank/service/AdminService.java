package com.bank.service;

import com.bank.model.Category;
import com.bank.model.Currency;
import com.bank.model.Customer;
import com.bank.model.enums.CustomerStatus;
import com.bank.model.enums.Role;
import com.bank.repository.CategoryRepository;
import com.bank.repository.CurrencyRepository;
import com.bank.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final CurrencyRepository currencyRepository;

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
}