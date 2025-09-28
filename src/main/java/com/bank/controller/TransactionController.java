package com.bank.controller;

import com.bank.model.dto.TransactionFullDto;
import com.bank.model.dto.TransactionOverview;
import com.bank.model.enums.PeriodType;
import com.bank.model.request.TransactionRequest;
import com.bank.model.request.TransactionStatisticsRequest;
import com.bank.service.TransactionService;
import com.bank.model.dto.TransactionDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
	private final TransactionService transactionService;
	
	@PostMapping("/make")
	public TransactionFullDto makeTransaction(@RequestBody TransactionRequest transactionRequest, HttpSession session) {
	 return transactionService.makeTransaction(transactionRequest, session);
	}

	@GetMapping("/grouped")
	public List<TransactionDTO> getTransactions(
			@RequestParam Long userId,
			@RequestParam PeriodType periodType,
			HttpSession session
		) {
				checkAccess(userId, session);
				return transactionService.getTransactions(userId, periodType);
	}

	@PostMapping("/transfer")
	public String transferFunds(@RequestBody TransactionRequest transactionRequest, HttpSession session) {
	return transactionService.transferFunds(
			 transactionRequest.getSourceWalletId(),
			 transactionRequest.getDestinationWalletId(),
			 transactionRequest.getAmount(),
			 session
		);
	}

	@PutMapping("/{id}/disableRecurring")
	public String disableRecurring(@PathVariable Long id, HttpSession session) {
		transactionService.disableRecurring(id, session);
		return "Recurring disabled for transaction " + id;
	}

	@GetMapping("/statistics")
	public TransactionOverview getStatisticsForTimePeriod(@RequestBody TransactionStatisticsRequest transactionStatisticsRequest,
												   HttpSession session) {
		checkAccess(transactionStatisticsRequest.getUserId(), session);
		return transactionService.getStatisticsForTimePeriod(transactionStatisticsRequest);
	}

	private void checkAccess(Long userId, HttpSession session) {
		Object currentUserId = session.getAttribute("USER_ID");
		if (!currentUserId.equals(userId)) {
	 		throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
		}
	}
}
