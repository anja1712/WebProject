package com.bank.controller;

import com.bank.model.Category;
import com.bank.model.Currency;
import com.bank.model.Transaction;
import com.bank.model.dto.Dashboard;
import com.bank.model.dto.TransactionViewDto;
import com.bank.model.enums.Role;
import com.bank.model.enums.UserStatus;
import com.bank.model.request.CurrencyRequest;
import com.bank.model.request.MonitoringRequest;
import com.bank.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PutMapping("/users/{id}/status")
    public String changeUserStatus(@PathVariable Long id, @RequestParam UserStatus status, HttpServletRequest request) {
        checkAccess(request);
        return adminService.changeCustomerStatus(id, status);
    }

    @GetMapping("/users/{id}/transactions")
    public List<TransactionViewDto> getTransactionsForUser(@PathVariable Long id, HttpServletRequest request) {
        checkAccess(request);
        return adminService.getTransactionsForUser(id);
    }

    @PutMapping("/users/{id}/note")
    public String addAdminNote(@PathVariable Long id, @RequestParam String note, HttpServletRequest request) {
        checkAccess(request);
        return adminService.addAdminNote(id, note);
    }

    @DeleteMapping("/users/{id}/note")
    public String deleteAdminNote(@PathVariable Long id, HttpServletRequest request) {
        checkAccess(request);
        return adminService.deleteAdminNote(id);
    }

    @GetMapping("/categories")
    public List<Category> getAllCategories(HttpServletRequest request) {
        checkAccess(request);
        return adminService.getAllCategories();
    }

    @PostMapping("/categories")
    public Category addCategory(@RequestBody Category category, HttpServletRequest request) {
        checkAccess(request);
        return adminService.addCategory(category);
    }

    @DeleteMapping("/categories/{id}")
    public String deleteCategory(@PathVariable Long id, HttpServletRequest request) {
        checkAccess(request);
        return adminService.deleteCategory(id);
    }
    
    @GetMapping("/dashboard")
    public Dashboard getDashboard(HttpServletRequest request) {
        checkAccess(request);
        return adminService.getDashboard();
    }

    @PostMapping("/monitor")
    public List<Transaction> transactionMonitoring(@RequestBody MonitoringRequest monitoringRequest, HttpServletRequest request) {
        checkAccess(request);
        return adminService.transactionMonitoring(monitoringRequest);
    }

    @PostMapping("/currencies")
    public Currency addCurrency(@RequestBody CurrencyRequest currencyRequest, HttpServletRequest request) {
        checkAccess(request);
        return adminService.addCurrency(currencyRequest);
    }

    @GetMapping("/currencies/all")
    public List<Currency> getCurrencies() {
        return adminService.getCurrencies();
    }

    @DeleteMapping("/currencies/{id}/delete")
    public void deleteCurrency(@PathVariable Long id, HttpServletRequest request) {
        checkAccess(request);
        adminService.deleteCurrency(id);
    }

    private void checkAccess(HttpServletRequest request) {
        Object role = request.getAttribute("ROLE");
        if (role == null || !role.equals(Role.ADMIN.toString())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "API requires admin access");
        }
    }
}