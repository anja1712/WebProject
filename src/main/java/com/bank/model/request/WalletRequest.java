package com.bank.model.request;

import com.bank.model.enums.CurrencyType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class WalletRequest {
    private String name;
    private BigDecimal initialValue;
    private BigDecimal currentValue;
    private CurrencyType currencyType;
    //what address for
    private String address;
    private Long customerId;
    private Boolean savings;
}