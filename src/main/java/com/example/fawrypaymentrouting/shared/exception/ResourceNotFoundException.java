package com.example.fawrypaymentrouting.shared.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseBusinessException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}

