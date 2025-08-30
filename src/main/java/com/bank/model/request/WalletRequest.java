package com.bank.model.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WalletRequest {
    private String name;
    private BigDecimal initialValue;
    private BigDecimal currentValue;
    private String currency;
    private String address;
    private Long customerId;
    private Boolean savings;
}