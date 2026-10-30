package com.example.clichepiggybank.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.FORBIDDEN) // Forces Spring to map this to 403
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(UUID id) {
        super("Inquirer with id '" + id + "' did not have enough rights to perform the request.");
    }
}
