package com.bank.repository;

import com.bank.model.Category;
import com.bank.model.enums.TransactionType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.validation.constraints.NotNull;
import java.util.List;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Long> {

    @Override
    List<Category> findAll();

    Category getCategoryByName(@NotNull String name);

    List<Category> findByUserId(@NotNull Long userId);

    List<Category> findByPredefinedTrue();

    Category findByUserIdAndNameAndTransactionType(@NotNull Long userId, @NotNull String name, TransactionType transactionType);

    Category findByNameAndTransactionTypeAndPredefinedTrue(@NotNull String name, TransactionType transactionType);

    Category getCategoryById(@NotNull long id);
}
