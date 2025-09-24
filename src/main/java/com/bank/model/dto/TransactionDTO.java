package com.bank.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class TransactionDTO {
	private Instant periodStart;
	private BigDecimal totalAmount;
	private Long transactionCount;
}
