package com.irkdoska.irkdoska.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class ExpiredTmaException extends Exception {
    
    private final String expiredToken;

    public ExpiredTmaException(String message, String expiredToken) {
        super(message);
        this.expiredToken = expiredToken;
    }

    public String getExpiredToken() {
        return expiredToken;
    }
}
