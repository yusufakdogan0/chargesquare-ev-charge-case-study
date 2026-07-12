package com.yusufakdogan.session_service.exception;

public class SessionNotActiveException extends RuntimeException {

    public SessionNotActiveException(String message) {
        super(message);
    }
}
