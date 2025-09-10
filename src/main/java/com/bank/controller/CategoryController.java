package com.bank.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.model.Category;
import com.bank.request.CategoryRequest;
import com.bank.service.CategoryService;
import com.bank.service.WalletService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CategoryController {
	private final CategoryService categoryService;
	
	@PostMapping("category/create")
	public Category addCategory(@RequestBody CategoryRequest request) {
		return categoryService.addCategory(request);
	}
	
	@GetMapping("/customer/{customerId}")
	public List<Category> getCustomerCategories(@PathVariable Long customerId){
		return categoryService.getCustomerCategories(customerId);
	}
	
	@PutMapping("/{categoryId}/update")
    public Category updateCategory(
            @PathVariable Long categoryId,
            @RequestBody CategoryRequest request,
            @RequestParam Long customerId
    ) {
        return categoryService.updateCategory(categoryId, request, customerId);
    }
	
}
