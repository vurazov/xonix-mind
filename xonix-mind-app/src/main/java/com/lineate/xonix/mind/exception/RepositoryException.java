package com.lineate.xonix.mind.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class RepositoryException extends ServiceException {

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public RepositoryException(Throwable throwable) {
        super(throwable);
    }
}
