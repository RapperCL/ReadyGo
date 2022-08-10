package com.cy.readygo.core.exception;

public class RedisXException extends RuntimeException{
    private static final long serialVersionUID = -2946266495682282677L;

    public RedisXException(String message) {
        super(message);
    }

    public RedisXException(Throwable e) {
        super(e);
    }

    public RedisXException(String message, Throwable cause) {
        super(message, cause);
    }
}
