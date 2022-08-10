package com.cy.readygo.core.RPCCommand;

public interface RPCCommand {

    void set(String key, String value);

    void get(String key);

    void rpc(String key, String value);
}
