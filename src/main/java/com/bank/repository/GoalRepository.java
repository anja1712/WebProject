package com.bank.repository;

import com.bank.model.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findAllByUserId(Long userId);

    Goal getGoalByName(String name);

    List<Goal> findByWalletId(@NotNull long id);
}
