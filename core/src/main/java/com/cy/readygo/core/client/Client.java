package com.cy.readygo.core.client;



import com.cy.readygo.core.RPCCommand.RPCCommand;
import com.cy.readygo.core.encode.SafeEncoder;
import com.cy.readygo.core.socket.RedisXSocketFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

public class Client extends BinaryXClient implements RPCCommand {

    public Client(String host) {
        super(host);
    }

    public Client(String host, int port) {
        super(host, port);
    }

    public Client(String host, int port, boolean ssl) {
        super(host, port, ssl);
    }

    public Client(String host, int port, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        super(host, port, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
    }

    public Client(RedisXSocketFactory redisXSocketFactory) {
        super(redisXSocketFactory);
    }

    @Override
    public void set(String key, String value) {
        this.set(SafeEncoder.encode(key), SafeEncoder.encode(value));
    }

    @Override
    public void get(String key) {
       this.get(SafeEncoder.encode(key));
    }

    @Override
    public void rpc(String key, String value) {
       this.rpc(SafeEncoder.encode(key), SafeEncoder.encode(value));
    }
}
