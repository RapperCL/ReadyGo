package com.cy.readygo.core.exception;


public class InvalidURIException extends RedisXException{
        private static final long serialVersionUID = -781691993326357802L;

        public InvalidURIException(String message) {
            super(message);
        }

        public InvalidURIException(Throwable cause) {
            super(cause);
        }

        public InvalidURIException(String message, Throwable cause) {
            super(message, cause);
        }

}
