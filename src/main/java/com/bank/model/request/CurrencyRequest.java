package com.bank.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class CurrencyRequest {
    private String name;
    private BigDecimal exchangeRateToDollar;
}
