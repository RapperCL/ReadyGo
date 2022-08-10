package com.cy.readygo.core.invocation;

public interface Invocation {

    String getServiceName();

    String getMethodName();

    String getData();

    default String getProtocol(){
        return "redis/json";
    }

    long getSid();
}
