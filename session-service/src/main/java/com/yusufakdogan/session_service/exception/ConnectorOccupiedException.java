package com.yusufakdogan.session_service.exception;

public class ConnectorOccupiedException extends RuntimeException {
    public ConnectorOccupiedException(String message) {
        super(message);
    }
}
