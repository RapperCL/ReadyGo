package com.cy.readygo.core.exception;

public class RedisXConnectionException extends RedisXException{
    private static final long serialVersionUID = 3878126572474819403L;

    public RedisXConnectionException(String message) {
        super(message);
    }

    public RedisXConnectionException(Throwable cause) {
        super(cause);
    }

    public RedisXConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
