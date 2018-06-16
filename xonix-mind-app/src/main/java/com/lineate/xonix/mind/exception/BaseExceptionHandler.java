package com.lineate.xonix.mind.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.EntityNotFoundException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Slf4j
public class BaseExceptionHandler extends ResponseEntityExceptionHandler {
    /**
     * Handle MissingServletRequestParameterException. Triggered when a 'required' request parameter is missing.
     *
     * @param exception MissingServletRequestParameterException
     * @param headers   HttpHeaders
     * @param status    HttpStatus
     * @param request   WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        return responseEntity(new ApiError(BAD_REQUEST,
                exception.getParameterName()
                        + " parameter is missing", exception));
    }

    /**
     * Handle HttpMediaTypeNotSupportedException. This one triggers when JSON is invalid as well.
     *
     * @param exception HttpMediaTypeNotSupportedException
     * @param headers   HttpHeaders
     * @param status    HttpStatus
     * @param request   WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        StringBuilder error = new StringBuilder();
        error.append(exception.getContentType());
        error.append(" media type is not supported. Supported media types are ");
        exception.getSupportedMediaTypes().forEach(t -> error.append(t).append(", "));
        return responseEntity(new ApiError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, error.toString(), exception));
    }

    /**
     * Handle HttpMessageNotReadableException. Happens when request JSON is malformed.
     *
     * @param exception HttpMessageNotReadableException
     * @param headers   HttpHeaders
     * @param status    HttpStatus
     * @param request   WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        return responseEntity(new ApiError(HttpStatus.BAD_REQUEST,
                "Malformed JSON Readable Exception", exception));
    }

    /**
     * Handle HttpMessageNotWritableException.
     *
     * @param exception HttpMessageNotWritableException
     * @param headers   HttpHeaders
     * @param status    HttpStatus
     * @param request   WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(
            HttpMessageNotWritableException exception,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        return responseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR,
                "Error writing JSON Writable Exception", exception));
    }


    /**
     * Handle HttpMessageNotWritableException.
     *
     * @param exception ArgumentTypeMismatchException
     * @param request   WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            WebRequest request) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage(String.format("The parameter '%s' of value '%s' could not be converted to type '%s'",
                exception.getName(), exception.getValue(),
                exception.getRequiredType().getSimpleName()));
        apiError.setDebugMessage(exception.getMessage());
        return responseEntity(apiError);
    }

    /**
     * Handles EntityNotFoundException. Created to encapsulate errors with more detail
     * than javax.persistence.EntityNotFoundException.
     *
     * @param exception the EntityNotFoundException
     * @return the ApiError object
     */
    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException exception) {
        return responseEntity(new ApiError(HttpStatus.NOT_FOUND, exception));
    }


    @ExceptionHandler(GamePlayException.class)
    protected ResponseEntity<Object> handleGamePlayException(GamePlayException exception) {
        return responseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, exception));
    }

    /**
     * Handle Exception, handle generic Exception.class
     *
     * @param exception the Exception
     * @return the ApiError object
     */
    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleGeneralException(Exception exception, WebRequest request) {
        return responseEntity(new ApiError(HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getMessage(), " error occurred!!!"));
    }

    private ResponseEntity<Object> responseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
