package com.bank.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder // make it quicker
public class TransactionOverview {
    private BigDecimal income;
    private BigDecimal expense;
    private Map<String, BigDecimal> transactionsByCategories;
    private List<Map.Entry<String, BigDecimal>> topTenExpenses;
}