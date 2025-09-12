package com.bank.controller;

import com.bank.model.Category;
import com.bank.model.Currency;
import com.bank.model.enums.CustomerStatus;
import com.bank.service.AdminService;

import dto.Dashboard;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PutMapping("/users/{id}/status")
    public String changeCustomerStatus(
            @PathVariable Long id,
            @RequestParam CustomerStatus status
    ) {
        return adminService.changeCustomerStatus(id, status);
    }

    @PutMapping("/users/{id}/note")
    public String addAdminNote(
            @PathVariable Long id,
            @RequestParam String note
    ) {
        return adminService.addAdminNote(id, note);
    }

    @PostMapping("/categories")
    public Category addCategory(@RequestBody Category category) {
        return adminService.addCategory(category);
    }

    @DeleteMapping("/categories/{name}")
    public String deleteCategory(@PathVariable String name) {
        return adminService.deleteCategory(name);
    }

    @PostMapping("/currencies")
    public Currency addCurrency(@RequestBody Currency currency) {
        return adminService.addCurrency(currency);
    }
    
    @GetMapping("/dashboard")
    public Dashboard getDashboard() {
        return adminService.getDashboard();
    }
}