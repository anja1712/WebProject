package com.bank.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionStatisticsRequest {
    @NotNull
    private Long userId;
    private Instant startDate;
    private Instant endDate;
    private String currency;
}