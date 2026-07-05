package com.personal.identity.application.exception;

import com.personal.identity.account.exception.DuplicateEmailException;
import com.personal.identity.account.exception.DuplicateUsernameException;
import com.personal.identity.account.exception.IdentityUserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {



    @ExceptionHandler(IdentityUserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            IdentityUserNotFoundException exception) {

        ApiErrorResponse response =
                new ApiErrorResponse(
                        "USER_NOT_FOUND",
                        exception.getMessage(),
                        Instant.now());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler({
            DuplicateUsernameException.class,
            DuplicateEmailException.class
    })
    public ResponseEntity<ApiErrorResponse> handleConflict(
            RuntimeException exception) {

        ApiErrorResponse response =
                new ApiErrorResponse(
                        "RESOURCE_CONFLICT",
                        exception.getMessage(),
                        Instant.now());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }
}
