package com.bank.service;

import com.bank.model.Category;
import com.bank.model.User;
import com.bank.model.dto.CategoryViewDto;
import com.bank.model.dto.UserDto;
import com.bank.model.enums.TransactionType;
import com.bank.model.request.UserRequest;
import com.bank.repository.CategoryRepository;
import com.bank.repository.UserRepository;
import com.bank.utils.DtoMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.bank.model.enums.Role.USER;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final DtoMapper dtoMapper;

    public User createUser(UserRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setBirthDate(request.getBirthDate());
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setBlocked(false);
        user.setRole(USER);
        user.setProfilePicture("https://profile.png");

        userRepository.save(user);

        return user;
    }

    public UserDto updateUser(Long id, User update) {
        User existingUser = userRepository.getUserById(id);
        if (existingUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user with id " + id + " doesn't exist");
        }

        existingUser.setFirstName(update.getFirstName() != null ? update.getFirstName() : existingUser.getFirstName());
        existingUser.setLastName(update.getLastName() != null ? update.getLastName() : existingUser.getLastName());
        existingUser.setUserName(update.getUserName() != null ? update.getUserName() : existingUser.getUserName());
        existingUser.setBirthDate(update.getBirthDate() != null ? update.getBirthDate() : existingUser.getBirthDate());
        existingUser.setEmail(update.getEmail() != null ? update.getEmail() : existingUser.getEmail());
        existingUser.setPassword(update.getPassword() != null ? update.getPassword() : existingUser.getPassword());
        existingUser.setProfilePicture(update.getProfilePicture() != null ? update.getProfilePicture() : existingUser.getProfilePicture());

        userRepository.save(existingUser);

        return dtoMapper.toDto(existingUser);
    }

    public User getUserById(Long id) {
        User user = userRepository.getUserById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id " + id + " doesn't exist");
        }

        return userRepository.getUserById(id);
    }

    public long getUserCount() {
        return userRepository.count();
    }


    public CategoryViewDto addCategory(String name, TransactionType type, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");

        Category predefined = categoryRepository.findByNameAndTransactionTypeAndPredefinedTrue(name, type);
        if (predefined != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Predefined category already exists: " + name + ", " + type);
        }

        Category custom = categoryRepository.findByUserIdAndNameAndTransactionType(userId, name, type);
        if (custom != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have custom category: " + name + ", " + type);
        }

        Category newCategory = new Category();
        newCategory.setName(name);
        newCategory.setTransactionType(type);
        newCategory.setCreator(USER);
        newCategory.setUserId(userId);
        newCategory.setPredefined(false);
        categoryRepository.save(newCategory);

        return new CategoryViewDto(newCategory.getName(), newCategory.getTransactionType());
    }

    public List<CategoryViewDto> getUserCategories(boolean onlyMy, HttpSession session) {
        Long userId = (Long) session.getAttribute("USER_ID");

        List<Category> categories = categoryRepository.findByUserId(userId);

        if (!onlyMy) {
            categories.addAll(categoryRepository.findByPredefinedTrue());
        }

        if (categories == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No categories found");
        }

        return categories.stream()
                .map(c -> new CategoryViewDto(c.getName(), c.getTransactionType()))
                .toList();
    }
}
