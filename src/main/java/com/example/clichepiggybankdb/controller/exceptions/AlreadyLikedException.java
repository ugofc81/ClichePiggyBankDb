package com.example.clichepiggybankdb.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND) // Forces Spring to map this to 404
public class AlreadyLikedException extends RuntimeException {
    public AlreadyLikedException(UUID likedGuid, UUID likerGuid) {
        super("Object '" + likedGuid + "' is already liked by user '" + likerGuid + "' was not found.");
    }
}
