package com.bank.model.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WalletRequest {
    private String name;
    private BigDecimal initialAmount;
    private BigDecimal currentAmount;
    private String currency;
    private Long userId;
    private Boolean savings;
    private Boolean archived;
}