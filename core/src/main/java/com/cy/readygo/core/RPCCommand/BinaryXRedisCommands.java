package com.cy.readygo.core.RPCCommand;


import com.cy.readygo.core.utils.SetParams;

public interface BinaryXRedisCommands {

    String set(byte[] var1, byte[] var2);

    String set(byte[] var1, byte[] var2, SetParams var3);

    byte[] get(byte[] var1);

    byte[] rpc(byte[] var1, byte[] var2);
}
