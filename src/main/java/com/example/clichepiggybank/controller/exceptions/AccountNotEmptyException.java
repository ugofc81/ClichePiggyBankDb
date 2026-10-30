package com.example.clichepiggybank.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND) // Forces Spring to map this to 404
public class AccountNotEmptyException extends RuntimeException {
    public AccountNotEmptyException(UUID id) {
        super("Account '" + id + "' has a positive balance.");
    }
}
