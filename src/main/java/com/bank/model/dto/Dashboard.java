package com.bank.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class Dashboard {
    private BigInteger allUsers;
    private BigInteger activeUsers;
    private BigDecimal activeUserBalance;
    private BigDecimal totalBalance;
    private List<TransactionViewDto> topTenMonthlyTransactions;
    private List<TransactionViewDto> topTenTwoMinutesTransactions;
}