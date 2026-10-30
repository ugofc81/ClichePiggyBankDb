package com.example.clichepiggybank.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND) // Forces Spring to map this to 404
public class LikeNotFoundException extends RuntimeException {
    public LikeNotFoundException(UUID likedGuid, UUID likerGuid) {
        super("Like of object '" + likedGuid + "' by user '" + likerGuid + "' was not found.");
    }
}
