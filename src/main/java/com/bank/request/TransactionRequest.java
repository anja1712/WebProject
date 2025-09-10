package com.bank.request;

import java.math.BigDecimal;
import java.sql.Date;

import com.bank.model.Category;
//import com.bank.model.Wallet;
import com.bank.model.enums.AccountType;
import com.bank.model.enums.Frequency;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionRequest {
	private String name;
    private BigDecimal amount;
    private Category category;
    private Long categoryId;
    private AccountType type;
    private Date transactionDate;
    private long walletId;
    private long fromWalletId;
    private long toWalletId;
    private long customerId;
    private Boolean recurring;
    private Frequency frequency;
}
