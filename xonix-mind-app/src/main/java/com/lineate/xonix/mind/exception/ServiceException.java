package com.lineate.xonix.mind.exception;

import lombok.NonNull;

public class ServiceException extends RuntimeException { //TODO: Class Exception should be here

    public ServiceException(@NonNull String message) {
        super(message);
    }

    public ServiceException(@NonNull String message, @NonNull Throwable throwable) {
        super(message, throwable);
    }

    public ServiceException(@NonNull Throwable throwable) {
        super(throwable);
    }

    public ServiceException() {
        this("ServiceException!");
    }
}
