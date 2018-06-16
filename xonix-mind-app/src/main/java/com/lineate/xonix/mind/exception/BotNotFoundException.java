package com.lineate.xonix.mind.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.persistence.EntityNotFoundException;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BotNotFoundException extends EntityNotFoundException {
    public BotNotFoundException(String message) {
        super(message);
    }
}