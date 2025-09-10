package dto;

import java.math.BigDecimal;
import java.time.LocalDate;
//import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WalletRequestDTO {
	private Long customerId;
	private	String name;
	private	BigDecimal initValue;
	private String currencyCode;
	private BigDecimal goalAmount;
	private LocalDate goalDeadline;
}
