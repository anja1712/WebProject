package com.bank.controller;

import com.bank.model.dto.GoalDto;
import com.bank.model.request.GoalRequest;
import com.bank.service.GoalService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
public class GoalController {
    private final GoalService goalService;

    @PostMapping("/create")
    public GoalDto createGoal(@RequestBody GoalRequest goalRequest, HttpServletRequest session) {
        return goalService.createGoal(goalRequest, session);
    }

    @PutMapping("/{id}/update")
    public GoalDto updateGoal(@PathVariable Long id, @RequestBody GoalRequest goalRequest, HttpServletRequest session) {
        return goalService.updateGoal(id, goalRequest, session);
    }

    @GetMapping("/{userId}/all")
    public List<GoalDto> getGoals(@PathVariable Long userId, HttpServletRequest session) {
        return goalService.getGoals(userId, session);
    }

    @DeleteMapping("/{id}")
    public String deleteGoal(@PathVariable Long id, HttpServletRequest session) {
        return goalService.deleteGoal(id, session);
    }
}
