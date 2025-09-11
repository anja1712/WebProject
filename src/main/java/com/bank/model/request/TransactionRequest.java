package com.bank.model.request;

import com.bank.model.enums.Frequency;
import com.bank.model.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;

@Data
@Builder
public class TransactionRequest {
	private String name;
    private String currency;
    private BigDecimal amount;
    private String category;
    private TransactionType type;
    private Date transactionDate;
    private Long walletId;
    private Long sourceWalletId;
    private Long destinationWalletId;
    private Long customerId;
    private Boolean recurring;
    private Frequency frequency;
}
