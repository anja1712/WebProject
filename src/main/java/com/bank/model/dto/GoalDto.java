package com.bank.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@Builder
public class GoalDto {
    private Long id;
    private String name;
    private Long userId;
    private Long walletId;
    private BigDecimal goalAmount;
    private BigDecimal currentAmount;
    private BigDecimal percentReached;
    private Instant startDate;
    private Instant targetDate;
}
