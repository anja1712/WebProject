package com.bank.model.dto;

import com.bank.model.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CategoryViewDto {
    private String name;
    private TransactionType transactionType;
}
