package com.bank.controller;

import com.bank.model.Customer;
import com.bank.model.request.CustomerRequest;
import com.bank.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping("/create")
    public Customer createCustomer(@RequestBody CustomerRequest request) {
        return customerService.createCustomer(request);
    }

    @PutMapping("/{id}/update")
    public Customer updateCustomer(@PathVariable("id") Long id, @RequestBody Customer update) {
        return customerService.updateCustomer(id, update);
    }

    @GetMapping("/count")
    public long getCustomerCount() {
       return customerService.getCustomerCount();
    }
}
