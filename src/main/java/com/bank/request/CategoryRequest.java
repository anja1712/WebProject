package com.bank.request;

import lombok.Data;

@Data
public class CategoryRequest {
	private Long customerId;
	private String name;
	private String type; //Admin ili user
}
