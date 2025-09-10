package com.bank.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bank.model.Category;
import com.bank.model.Customer;
import com.bank.model.enums.AccountType;
import com.bank.request.CategoryRequest;
import com.bank.repository.CategoryRepository;
import com.bank.repository.CustomerRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
	
	private final CategoryRepository categoryRepository;
	private final CustomerService customerService;
	private final CustomerRepository customerRepository;
	
	public Category addCategory(CategoryRequest request) {
		
		Customer customer = customerRepository.findById(request.getCustomerId())
				.orElseThrow(() -> new RuntimeException("Customer with id " + request.getCustomerId() + " not found"));
		
		//Parsiranje AccountType-a 
		AccountType type;
		try {
			type = AccountType.valueOf(request.getType().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Invalid account type");
		}
		
		Category category = new Category();
		category.setName(request.getName());
		category.setType(type);
		category.setPredefined(false);
		category.setCustomer(customer);
		
		return categoryRepository.save(category);
	}
	
	public List<Category> getCustomerCategories(Long customerId){
		Customer customer = customerRepository.getCustomerById(customerId);
		if(customer == null) {
			throw new RuntimeException("Customer with id "+ customerId + " not found");
		}
		
		List<Category> categories = categoryRepository.getCategoriesById(customer.getId());
		if(categories == null) {
			throw new RuntimeException("Categories for customer " + customerId + " not found");
		}
		return categories;
	}
	
	public void createPredefinedCategories(Customer customer) {
		String[] names = {"Food", "Entertainment", "Household", "Investments"};
		
		for(String name : names){
			Category category = new Category();
			category.setName(name);
			category.setType(AccountType.OUTCOME);
			category.setPredefined(true);
			category.setCustomer(customer);
			
			categoryRepository.save(category);
		}
	}
	
	@Transactional
	public Category updateCategory(Long categoryId, CategoryRequest request, Long customerId) {
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(()-> new RuntimeException("Customer not found"));
		
		Category category = categoryRepository.findById(categoryId)
			.orElseThrow(()-> new RuntimeException("Category not found"));
		
		//ako je predefinisana i globalna (ne pripada nikome konkretno, .getCustomer()==null)
		if(category.getPredefined() != null && category.getPredefined() && category.getCustomer()==null) {
			Category newCategory = new Category();
			newCategory.setName(request.getName() != null ? request.getName() : category.getName());
			newCategory.setType(request.getType() != null ? AccountType.valueOf(request.getType().toUpperCase()) : category.getType());
			newCategory.setPredefined(false);
			newCategory.setCustomer(customer);
			
			return categoryRepository.save(newCategory);
		}
		
		//ako je korisnicka kategorija, proveri da li pripada korisniku
		if(category.getCustomer() == null || category.getCustomer().getId()!=customerId) {
			throw new RuntimeException("Not authorized to update this category");
		}
		
		
		//Izmena postojece kategorije
		if(request.getName() != null) 
			category.setName(request.getName());
		if(request.getType() != null) 
			category.setType(AccountType.valueOf(request.getType().toUpperCase()));
			
		return categoryRepository.save(category);
	}
	
	
	
}
