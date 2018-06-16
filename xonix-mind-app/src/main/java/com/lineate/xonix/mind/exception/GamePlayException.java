package com.lineate.xonix.mind.exception;

import lombok.NonNull;

public class GamePlayException extends ServiceException {

    public GamePlayException(@NonNull String message) {
        super(message);
    }

    public GamePlayException(@NonNull String message, @NonNull Throwable throwable) {
        super(message, throwable);
    }

}
