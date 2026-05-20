package com.example.fawrypaymentrouting.shared.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends BaseBusinessException {

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

