package com.bank.repository;

import com.bank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.validation.constraints.NotNull;

public interface UserRepository extends JpaRepository<User, Long> {

    User getUserById(@NotNull long id);

	Long countByBlockedFalse();

    User findByEmail(String email);
}