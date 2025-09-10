package dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionDTO {
	private Date periodStart;
	private BigDecimal totalAmount;
	private Long transactionCount;
}
