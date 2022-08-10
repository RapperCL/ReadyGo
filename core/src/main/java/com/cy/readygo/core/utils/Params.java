package com.cy.readygo.core.utils;



import com.cy.readygo.core.RPCCommand.RPCProtocol;
import com.cy.readygo.core.encode.SafeEncoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class Params {
    private Map<String, Object> params;

    public Params() {
    }

    public <T> T getParam(String name) {
        return this.params == null ? null : (T) this.params.get(name);
    }

    public byte[][] getByteParams() {
        if (this.params == null) {
            return new byte[0][];
        } else {
            ArrayList<byte[]> byteParams = new ArrayList();
            Iterator var2 = this.params.entrySet().iterator();

            while(var2.hasNext()) {
                Map.Entry<String, Object> param = (Map.Entry)var2.next();
                byteParams.add(SafeEncoder.encode((String)param.getKey()));
                Object value = param.getValue();
                if (value != null) {
                    if (value instanceof byte[]) {
                        byteParams.add((byte[])((byte[])value));
                    } else if (value instanceof Boolean) {
                        byteParams.add(RPCProtocol.toByteArray((Boolean)value));
                    } else if (value instanceof Integer) {
                        byteParams.add(RPCProtocol.toByteArray((Integer)value));
                    } else if (value instanceof Long) {
                        byteParams.add(RPCProtocol.toByteArray((Long)value));
                    } else if (value instanceof Double) {
                        byteParams.add(RPCProtocol.toByteArray((Double)value));
                    } else {
                        byteParams.add(SafeEncoder.encode(String.valueOf(value)));
                    }
                }
            }

            return (byte[][])byteParams.toArray(new byte[byteParams.size()][]);
        }
    }

    protected boolean contains(String name) {
        return this.params == null ? false : this.params.containsKey(name);
    }

    protected void addParam(String name, Object value) {
        if (this.params == null) {
            this.params = new HashMap();
        }

        this.params.put(name, value);
    }

    protected void addParam(String name) {
        if (this.params == null) {
            this.params = new HashMap();
        }

        this.params.put(name, (Object)null);
    }
}
