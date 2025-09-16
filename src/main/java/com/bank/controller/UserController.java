package com.bank.controller;

import com.bank.model.User;
import com.bank.model.dto.CategoryViewDto;
import com.bank.model.enums.TransactionType;
import com.bank.model.request.UserRequest;
import com.bank.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.bank.model.enums.Role.ADMIN;
import static com.bank.model.enums.Role.USER;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/create")
    public User createUser(@RequestBody UserRequest request, HttpSession session) {
        if (session.getAttribute("ROLE") != ADMIN) {
            throw new RuntimeException("Admin access required");
        }
        return userService.createUser(request);
    }

    @PutMapping("/{id}/update")
    public User updateUser(@PathVariable("id") Long id, @RequestBody User update, HttpSession session) {
        Object role = session.getAttribute("ROLE");
        Object currentUserId = session.getAttribute("USER_ID");

        if (role == USER) {
            if (currentUserId != id) {
                throw new RuntimeException("Access denied");
            }
        }

        User user = userService.updateUser(id, update);

        if (user.getAdminNote() != null) {
            if (session.getAttribute("ROLE") != ADMIN) {
                user.setAdminNote(null);
            }
        }

        return user;
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable("id") Long id, HttpSession session) {
        Object role = session.getAttribute("ROLE");
        Object currentUserId = session.getAttribute("USER_ID");

        if (role == USER) {
            if (currentUserId != id) {
                throw new RuntimeException("Access denied");
            }
        }

        User user = userService.getUserById(id);

        if (user.getAdminNote() != null) {
            if (session.getAttribute("ROLE") != ADMIN) {
                user.setAdminNote(null);
            }
        }

        return user;
    }

    @GetMapping("/count")
    public long getUserCount() {
       return userService.getUserCount();
    }

    @PostMapping("categories/add")
    public CategoryViewDto addCategory(@RequestParam String name, @RequestParam TransactionType type, HttpSession session) {
        return userService.addCategory(name, type, session);
    }

    @GetMapping("/categories")
    public List<CategoryViewDto> getUserCategories(@RequestParam boolean onlyMy, HttpSession session) {
       return userService.getUserCategories(onlyMy, session);
    }
}
