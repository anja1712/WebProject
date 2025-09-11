package com.bank.controller;

import com.bank.model.Transaction;
import com.bank.model.dto.TransactionOverview;
import com.bank.model.enums.PeriodType;
import com.bank.model.request.TransactionRequest;
import com.bank.model.request.TransactionStatisticsRequest;
import com.bank.service.TransactionService;
import dto.TransactionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
	
	private final TransactionService transactionService;
	
	 @PostMapping("/make")
	 public Transaction makeTransaction(@RequestBody TransactionRequest transactionRequest) {
		 return transactionService.makeTransaction(transactionRequest);
	 }
	
	 
	 @GetMapping("/grouped")
	    public List<TransactionDTO> getTransactions(
	            @RequestParam Long customerId,
	            @RequestParam PeriodType periodType
	    ) {
	        return transactionService.getTransaction(customerId, periodType);
	    }
	 
	 @PostMapping("/transfer")
	 public String transferFunds(@RequestBody TransactionRequest transactionRequest) {
	     return transactionService.transferFunds(
				 transactionRequest.getSourceWalletId(),
				 transactionRequest.getDestinationWalletId(),
				 transactionRequest.getAmount()
		 );
	 }
	 
	 @PutMapping("/{id}/disableRecurring")
	 public String disableRecurring(@PathVariable Long id) {
	     transactionService.disableRecurring(id);
	     return "Recurring disabled for transaction " + id;
	 }

	 @GetMapping("/statistics")
	 public TransactionOverview getStatisticsForTimePeriod(@RequestBody TransactionStatisticsRequest transactionStatisticsRequest) {
		 return transactionService.getStatisticsForTimePeriod(transactionStatisticsRequest);
	 }
}
