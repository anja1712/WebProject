package dto;

import java.math.BigDecimal;
import java.time.LocalDate;
//import java.util.Date;
import java.util.Date;

import com.bank.model.enums.AccountType;
import com.bank.model.enums.Frequency;

import lombok.Data;

@Data
public class TransactionRequestDTO {
	private String name;
	private	BigDecimal amount;
	private AccountType type;
	private Long categoryId;
	private	Date transactionDate;
	private Boolean recurring;
	private Frequency frequency;
	private Long walletId;
	private Long customerId;
	
}
