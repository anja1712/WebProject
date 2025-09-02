package com.bank.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.model.Transaction;
//import com.bank.model.Wallet;
import com.bank.model.enums.PeriodType;
import com.bank.model.request.TransactionRequest;
//import com.bank.model.request.WalletRequest;
import com.bank.service.TransactionService;

import dto.TransactionDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor

public class TransactionController {
	
	private final TransactionService transactionService;
	
	 @PostMapping("transaction/create")
	 public Transaction addTransaction(@RequestBody TransactionRequest transactionRequest) { return transactionService.addTransaction(transactionRequest); }
	
	 
	 @GetMapping("/grouped")
	    public List<TransactionDTO> getTransactions(
	            @RequestParam Long customerId,
	            @RequestParam PeriodType periodType
	    ) {
	        return transactionService.getTransaction(customerId, periodType);
	    }
	 
	 @PostMapping("/transfer")
	 public String transferFunds(@RequestBody TransactionRequest transactionRequest) {
	    transactionService.transferFunds(
	    transactionRequest.getFromWalletId(),
	    transactionRequest.getToWalletId(),
	    transactionRequest.getAmount()
	     );
	     return "Transfer successful";
	 }
	 
	 @PostMapping("/transaction/{id}/disableRecurring")
	 public String disableRecurring(@PathVariable Long id) {
	     transactionService.disableRecurring(id);
	     return "Recurring disabled for transaction " + id;
	 }

}
