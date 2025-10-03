package com.bank.controller;

import com.bank.model.dto.TransactionFullDto;
import com.bank.model.dto.TransactionOverview;
import com.bank.model.enums.PeriodType;
import com.bank.model.request.TransactionRequest;
import com.bank.model.request.TransactionStatisticsRequest;
import com.bank.service.TransactionService;
import com.bank.model.dto.TransactionDTO;
import jakarta.servlet.http.HttpServletRequest;
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

	private static final int LIMIT = 20;
	
	@PostMapping("/make")
	public TransactionFullDto makeTransaction(@RequestBody TransactionRequest transactionRequest, HttpServletRequest session) {
	 return transactionService.makeTransaction(transactionRequest, session);
	}

	@GetMapping("/wallets/{walletId}")
	public List<TransactionFullDto> getWalletTransactions(@PathVariable Long walletId, HttpServletRequest session) {
		return transactionService.getWalletTransactions(walletId, session);
	}

	@GetMapping("/users/{userId}/last")
	public List<TransactionFullDto> getUsersLastTransactions(@PathVariable Long userId) {
		return transactionService.getUsersLastTransactions(userId, LIMIT);
	}

	@GetMapping("/grouped")
	public List<TransactionDTO> getTransactions(
			@RequestParam Long userId,
			@RequestParam PeriodType periodType,
			HttpServletRequest session
		) {
				checkAccess(userId, session);
				return transactionService.getTransactions(userId, periodType);
	}

	@PostMapping("/transfer")
	public String transferFunds(@RequestBody TransactionRequest transactionRequest, HttpServletRequest session) {
	return transactionService.transferFunds(
			 transactionRequest.getSourceWalletId(),
			 transactionRequest.getDestinationWalletId(),
			 transactionRequest.getAmount(),
			 session
		);
	}

	@PutMapping("/{id}/disableRecurring")
	public String disableRecurring(@PathVariable Long id, HttpServletRequest session) {
		transactionService.disableRecurring(id, session);
		return "Recurring disabled for transaction " + id;
	}

	@PostMapping("/statistics")
	public TransactionOverview getStatisticsForTimePeriod(@RequestBody TransactionStatisticsRequest transactionStatisticsRequest,
												   HttpServletRequest session) {
		checkAccess(transactionStatisticsRequest.getUserId(), session);
		return transactionService.getStatisticsForTimePeriod(transactionStatisticsRequest);
	}

	private void checkAccess(Long userId, HttpServletRequest session) {
		Object currentUserId = session.getAttribute("USER_ID");
		if (!currentUserId.equals(userId)) {
	 		throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
		}
	}
}
