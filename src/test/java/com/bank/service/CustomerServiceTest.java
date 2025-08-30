package com.bank.service;

import com.bank.model.Customer;
import com.bank.model.enums.Role;
import com.bank.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CustomerServiceTest{
    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    @Test
    void testGetCustomerCount() {
        Customer c1 = new Customer();
        c1.setBirthDate(new Date());
        c1.setCreationDate(new Date());
        c1.setBlocked(false);
        c1.setEmail("user1@example.com");
        c1.setRole(Role.USER);
        c1.setUserName("user1");
        c1.setPassword("password1");
        c1.setProfilePicture("pic1.jpg");
        c1.setFirstName("John");
        c1.setLastName("Doe");

        Customer c2 = new Customer();
        c2.setBirthDate(new Date());
        c2.setCreationDate(new Date());
        c2.setBlocked(false);
        c2.setEmail("user2@example.com");
        c2.setRole(Role.USER);
        c2.setUserName("user2");
        c2.setPassword("password2");
        c2.setProfilePicture("pic2.jpg");
        c2.setFirstName("Jane");
        c2.setLastName("Smith");

        customerRepository.save(c1);
        customerRepository.save(c2);

        long count = customerService.getCustomerCount();

        assertThat(count).isEqualTo(2);
    }
}
