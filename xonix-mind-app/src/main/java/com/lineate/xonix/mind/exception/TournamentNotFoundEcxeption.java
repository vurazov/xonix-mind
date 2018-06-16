package com.lineate.xonix.mind.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.persistence.EntityNotFoundException;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TournamentNotFoundEcxeption extends EntityNotFoundException {

    public TournamentNotFoundEcxeption(String message) {
        super(message);
    }
}
