package com.bank.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public class GoalRequest {
    private String name;
    private Long walletId;
    private BigDecimal goalAmount;
    private Instant targetDate;
}
