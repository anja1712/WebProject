package com.bank.repository;

import com.bank.model.Category;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import javax.validation.constraints.NotNull;

@Repository
public interface CategoryRepository extends CrudRepository<Category, Long> {

    Category getCategoryById(@NotNull long id);

	List<Category> getCategoriesById(long customerId);
    
}
