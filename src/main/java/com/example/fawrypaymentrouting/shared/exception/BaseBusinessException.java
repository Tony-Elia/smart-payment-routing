package com.example.fawrypaymentrouting.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseBusinessException extends RuntimeException {

    private final HttpStatus status;

    public BaseBusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public BaseBusinessException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

}

