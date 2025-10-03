package com.bank.model.dto;

import com.bank.model.enums.Frequency;
import com.bank.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public class TransactionFullDto {
    private Long id;
    private String name;
    private String currency;
    private String category;
    private TransactionType type;
    private BigDecimal amount;
    private Long walletId;
    private Long userId;
    private Instant transactionDate;
    private Boolean recurring;
    private Frequency frequency;
}
