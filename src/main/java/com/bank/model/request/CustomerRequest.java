package com.bank.model.request;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class CustomerRequest {
    private String firstName;
    private String lastName;
    private String userName;
    private Date birthDate;
    private String email;
    private String password;
}
