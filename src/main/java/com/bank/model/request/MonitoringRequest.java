package com.bank.model.request;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class MonitoringRequest {
    private Long userId;
    private String category;
    private BigDecimal amount;
    private boolean amountGreaterOrEqual;
    private Instant date;
    private boolean dateAfterOrEqual;
    private boolean sortByDate;
    private boolean sortByAmount;
}
