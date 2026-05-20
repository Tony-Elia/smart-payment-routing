package com.example.fawrypaymentrouting.shared.exception;

import org.springframework.http.HttpStatus;

public class ResourceConflictException extends BaseBusinessException {

    public ResourceConflictException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}

