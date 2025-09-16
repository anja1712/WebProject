package com.bank.service;

import com.bank.model.User;
import com.bank.model.enums.Role;
import com.bank.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceTest{
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testGetUserCount() {
        User u1 = new User();
        u1.setBirthDate(Instant.now());
        u1.setCreated(Instant.now());
        u1.setBlocked(false);
        u1.setEmail("user1@example.com");
        u1.setRole(Role.USER);
        u1.setUserName("user1");
        u1.setPassword("password1");
        u1.setProfilePicture("pic1.jpg");
        u1.setFirstName("John");
        u1.setLastName("Doe");

        User u2 = new User();
        u2.setBirthDate(Instant.now());
        u2.setCreated(Instant.now());
        u2.setBlocked(false);
        u2.setEmail("user2@example.com");
        u2.setRole(Role.USER);
        u2.setUserName("user2");
        u2.setPassword("password2");
        u2.setProfilePicture("pic2.jpg");
        u2.setFirstName("Jane");
        u2.setLastName("Smith");

        userRepository.save(u1);
        userRepository.save(u2);

        long count = userService.getUserCount();

        assertThat(count).isEqualTo(2);
    }
}
