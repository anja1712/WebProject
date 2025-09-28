package com.bank.model.dto;

import com.bank.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public class TransactionViewDto {
    private String name;
    private TransactionType type;
    private String currency;
    private BigDecimal amount;
    private Instant date;
}
