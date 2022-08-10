package com.cy.readygo.core.utils;



import com.cy.readygo.core.RPCCommand.RPCProtocol;
import com.cy.readygo.core.encode.SafeEncoder;

import java.util.ArrayList;

public class SetParams extends Params {
    private static final String XX = "xx";
    private static final String NX = "nx";
    private static final String PX = "px";
    private static final String EX = "ex";

    public SetParams() {
    }

    public static  SetParams setParams() {
        return new  SetParams();
    }

    public  SetParams ex(int secondsToExpire) {
        this.addParam("ex", secondsToExpire);
        return this;
    }

    public  SetParams px(long millisecondsToExpire) {
        this.addParam("px", millisecondsToExpire);
        return this;
    }

    public  SetParams nx() {
        this.addParam("nx");
        return this;
    }

    public  SetParams xx() {
        this.addParam("xx");
        return this;
    }

    public byte[][] getByteParams(byte[]... args) {
        ArrayList<byte[]> byteParams = new ArrayList();
        byte[][] var3 = args;
        int var4 = args.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            byte[] arg = var3[var5];
            byteParams.add(arg);
        }

        if (this.contains("nx")) {
            byteParams.add(SafeEncoder.encode("nx"));
        }

        if (this.contains("xx")) {
            byteParams.add(SafeEncoder.encode("xx"));
        }

        if (this.contains("ex")) {
            byteParams.add(SafeEncoder.encode("ex"));
            byteParams.add(RPCProtocol.toByteArray((Integer)this.getParam("ex")));
        }

        if (this.contains("px")) {
            byteParams.add(SafeEncoder.encode("px"));
            byteParams.add(RPCProtocol.toByteArray((Long)this.getParam("px")));
        }

        return (byte[][])byteParams.toArray(new byte[byteParams.size()][]);
    }
}
