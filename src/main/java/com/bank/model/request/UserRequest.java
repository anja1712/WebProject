package com.bank.model.request;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserRequest {
    private String firstName;
    private String lastName;
    private String userName;
    private Instant birthDate;
    private String email;
    private String password;
}
