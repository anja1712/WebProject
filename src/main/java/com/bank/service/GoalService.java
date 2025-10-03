package com.bank.service;

import com.bank.model.Goal;
import com.bank.model.Wallet;
import com.bank.model.dto.GoalDto;
import com.bank.model.request.GoalRequest;
import com.bank.repository.GoalRepository;
import com.bank.repository.UserRepository;
import com.bank.repository.WalletRepository;
import com.bank.utils.DtoMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoalService {
    private final GoalRepository goalRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final DtoMapper dtoMapper;

    public GoalDto createGoal(GoalRequest goalRequest, HttpServletRequest session) {
        Long userId = (Long) session.getAttribute("USER_ID");
        Wallet wallet = walletRepository.getWalletById(goalRequest.getWalletId());
        if (wallet == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Wallet with id " + goalRequest.getWalletId() + " not found");
        }

        if (!wallet.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User doesn't belong to user");
        }

        if (!wallet.getSavings()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Need to select savings wallet");
        }

        Goal goal = new Goal();
        goal.setName(goalRequest.getName());
        goal.setCurrentAmount(
                goalRequest.getCurrentAmount() != null
                        ? goalRequest.getCurrentAmount()
                        : BigDecimal.ZERO
        );
        goal.setGoalAmount(goalRequest.getGoalAmount());
        goal.setWallet(wallet);
        goal.setUser(userRepository.findById(userId).get());
        goal.setStartDate(Instant.now());
        goal.setTargetDate(goalRequest.getTargetDate());

        goalRepository.save(goal);

        return dtoMapper.toDto(goal);
    }

    public GoalDto updateGoal(Long goalId, GoalRequest request, HttpServletRequest session) {
        Long userId = (Long) session.getAttribute("USER_ID");

        Optional<Goal> currentGoal = goalRepository.findById(goalId);
        if (currentGoal.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Goal with id " + goalId + " not found");
        }

        Goal goal = currentGoal.get();

        if (request.getName() != null) {
            goal.setName(request.getName());
        }

        if (request.getGoalAmount() != null) {
            goal.setGoalAmount(request.getGoalAmount());
        }

        if (request.getWalletId() != null) {
            Wallet wallet = walletRepository.getWalletById(goal.getWallet().getId());
            if (wallet.getUser().getId().equals(userId) && wallet.getSavings()) {
                goal.setWallet(wallet);
            }
        }

        if (request.getTargetDate() != null) {
            goal.setTargetDate(request.getTargetDate());
        }

        goalRepository.save(goal);

        return dtoMapper.toDto(goal);
    }

    public List<GoalDto> getGoals(Long userId, HttpServletRequest session) {
        Long currentUserId = (Long) session.getAttribute("USER_ID");

        if (!currentUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Can't access user's goals");
        }

        List<Goal> goals = goalRepository.findAllByUserId(userId);
        List<GoalDto> response = new ArrayList<>();
        if (!goals.isEmpty()) {
            response = goals.stream().map(dtoMapper::toDto).toList();
        }

        return response;
    }

    public String deleteGoal(Long id, HttpServletRequest session) {
        Long userId = (Long) session.getAttribute("USER_ID");

        Optional<Goal> currentGoal = goalRepository.findById(id);
        if (currentGoal.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Goal with id " + id + " not found");
        }

        Goal goal = currentGoal.get();
        if (!goal.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Goal doesn't belong to user");
        }

        goalRepository.delete(goal);

        return "Goal with id " + id + " deleted";
    }
}
