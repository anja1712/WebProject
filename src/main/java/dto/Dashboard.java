package dto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import com.bank.model.Transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Dashboard {
	private BigInteger allUsers;
	private BigInteger activeUsers;
	private BigDecimal activeUserBalance;
	private BigDecimal totalBalance;
	private List<Transaction> topTenMonthlyTransactions;
	private List<Transaction> topTenTwoMinutesTransactions;
}
