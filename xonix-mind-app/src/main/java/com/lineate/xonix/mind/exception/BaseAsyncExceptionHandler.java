package com.lineate.xonix.mind.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

@Slf4j
public class BaseAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(
            Throwable throwable, Method method, Object... obj) {
        StringBuilder error = new StringBuilder();
        error.append("Exception occurred::" + throwable.getMessage()).append('\n')
             .append("Method Name::"+method.getName()).append('\n');
        for (Object param : obj) {
            error.append("Parameter value - " + param).append('\n');
        }
        log.error("UncaughtException", error);
    }

}
