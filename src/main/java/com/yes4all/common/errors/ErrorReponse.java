package com.yes4all.common.errors;

import org.springframework.http.HttpStatus;

public class ErrorReponse {
    private HttpStatus status;

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    private String message;
}
