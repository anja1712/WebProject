package com.bank.service;

import com.bank.model.Customer;
import com.bank.model.enums.Role;
import com.bank.model.request.CustomerRequest;
import com.bank.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;

    public Customer createCustomer(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setBirthDate(request.getBirthDate());
        customer.setUserName(request.getUserName());
        customer.setEmail(request.getEmail());
        customer.setPassword(request.getPassword());
        customer.setBlocked(false);
        customer.setRole(Role.USER);
        customer.setProfilePicture("profile.png");

        //unless admin is registering a user
        customer.setAdminNote(null);

        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer update) {
        Customer existingCustomer = customerRepository.getCustomerById(id);
        if (existingCustomer == null) {
            throw new RuntimeException("Customer with id " + id + " doesn't exist");
        }

        existingCustomer.setFirstName(update.getFirstName());
        existingCustomer.setLastName(update.getLastName());
        existingCustomer.setBirthDate(update.getBirthDate());
        existingCustomer.setEmail(update.getEmail());
        existingCustomer.setPassword(update.getPassword());
        existingCustomer.setRole(update.getRole());
        existingCustomer.setProfilePicture(update.getProfilePicture());

        existingCustomer.setAdminNote(null);
        return customerRepository.save(existingCustomer);
    }

    public long getCustomerCount() {
        return customerRepository.count();
    }
}
