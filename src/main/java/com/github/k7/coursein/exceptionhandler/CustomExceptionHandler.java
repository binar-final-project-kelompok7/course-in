package com.github.k7.coursein.exceptionhandler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.github.k7.coursein.model.WebResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<WebResponse<String>> constraintViolationException(ConstraintViolationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(WebResponse.<String>builder().errors(exception.getMessage()).build());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<WebResponse<String>> apiException(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatus())
            .body(WebResponse.<String>builder().errors(exception.getReason()).build());
    }

    @ExceptionHandler(InvalidMediaTypeException.class)
    public ResponseEntity<WebResponse<String>> invalidMediaTypeException(InvalidMediaTypeException exception) {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(WebResponse.<String>builder().errors(exception.getMessage()).build());
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<WebResponse<String>> invalidFormatException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(WebResponse.<String>builder().errors("Wrong format data").build());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<WebResponse<String>> httpMessageNotReadableException() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(WebResponse.<String>builder().errors("Wrong format data").build());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<WebResponse<String>> usernameNotFoundException(UsernameNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(WebResponse.<String>builder().errors(exception.getMessage()).build());
    }

}
