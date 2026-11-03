package com.example.clichepiggybankdb.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.FORBIDDEN) // Forces Spring to map this to 403
public class GoneException extends RuntimeException {
    public GoneException(String str) {
        super("The operation requested '" + str + "' is not executable anymore.");
    }
}
