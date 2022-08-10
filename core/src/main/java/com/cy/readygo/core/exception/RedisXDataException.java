package com.cy.readygo.core.exception;

public class RedisXDataException extends RedisXException{
    private static final long serialVersionUID = 3878126572474819403L;

    public RedisXDataException(String message) {
        super(message);
    }

    public RedisXDataException(Throwable cause) {
        super(cause);
    }

    public RedisXDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
