package com.bank.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public class WalletDto {
    private Long id;
    private String name;
    private String currency;
    private Long userId;
    private BigDecimal balance;
    private Instant created;
    private Instant modified;
    private Boolean savings;
    private Boolean archived;
}
