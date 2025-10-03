package com.bank.model.request;

import com.bank.model.enums.Frequency;
import com.bank.model.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class TransactionRequest {
	private String name;
    private String currency;
    private BigDecimal amount;
    private String category;
    private TransactionType type;
    private Instant transactionDate;
    private Long walletId;
    private Long sourceWalletId;
    private Long destinationWalletId;
    private Boolean recurring;
    private Frequency frequency;
    private String goal;
}
