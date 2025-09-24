package com.bank.controller;

import com.bank.model.Category;
import com.bank.model.Currency;
import com.bank.model.Transaction;
import com.bank.model.dto.Dashboard;
import com.bank.model.dto.TransactionViewDto;
import com.bank.model.enums.Role;
import com.bank.model.enums.UserStatus;
import com.bank.model.request.MonitoringRequest;
import com.bank.service.AdminService;
import jakarta.servlet.http.HttpSession;
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
    public String changeUserStatus(@PathVariable Long id, @RequestParam UserStatus status, HttpSession session) {
        checkAccess(session);
        return adminService.changeCustomerStatus(id, status);
    }

    @GetMapping("/users/{id}/transactions")
    public List<TransactionViewDto> getTransactionsForUser(@PathVariable Long id, HttpSession session) {
        checkAccess(session);
        return adminService.getTransactionsForUser(id);
    }

    @PutMapping("/users/{id}/note")
    public String addAdminNote(@PathVariable Long id, @RequestParam String note, HttpSession session) {
        checkAccess(session);
        return adminService.addAdminNote(id, note);
    }

    @GetMapping("/categories")
    public List<Category> getAllCategories(HttpSession session) {
        checkAccess(session);
        return adminService.getAllCategories();
    }

    @PostMapping("/categories")
    public Category addCategory(@RequestBody Category category, HttpSession session) {
        checkAccess(session);
        return adminService.addCategory(category);
    }

    @DeleteMapping("/categories/{id}")
    public String deleteCategory(@PathVariable Long id, HttpSession session) {
        checkAccess(session);
        return adminService.deleteCategory(id);
    }

    @PostMapping("/currencies")
    public Currency addCurrency(@RequestBody Currency currency, HttpSession session) {
        checkAccess(session);
        return adminService.addCurrency(currency);
    }
    
    @GetMapping("/dashboard")
    public Dashboard getDashboard(@RequestParam String currency, HttpSession session) {
        checkAccess(session);
        return adminService.getDashboard(currency);
    }

    @GetMapping("/monitor")
    public List<Transaction> transactionMonitoring(@RequestBody MonitoringRequest request, HttpSession session) {
        checkAccess(session);
        return adminService.transactionMonitoring(request);
    }

    private void checkAccess(HttpSession session) {
        Object role = session.getAttribute("ROLE");
        if (role != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "API requires admin access");
        }
    }
}