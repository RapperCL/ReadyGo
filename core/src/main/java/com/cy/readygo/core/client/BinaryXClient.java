package com.cy.readygo.core.client;


import com.cy.readygo.core.RPCCommand.RPCProtocol;
import com.cy.readygo.core.connect.Connection;
import com.cy.readygo.core.socket.RedisXSocketFactory;
import com.cy.readygo.core.utils.SetParams;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;

public class BinaryXClient extends Connection {
    private boolean isInMulti;
    private String user;
    private String password;
    private int db;
    private boolean isInWatch;

    public BinaryXClient(){}

    public BinaryXClient(String host) {
        super(host);
    }

    public BinaryXClient(String host, int port) {
        super(host, port);
    }

    public BinaryXClient(String host, int port, boolean ssl) {
        super(host, port, ssl);
    }

    public BinaryXClient(String host, int port, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        super(host, port, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
    }

    public BinaryXClient(RedisXSocketFactory redisXSocketFactory) {
        super(redisXSocketFactory);
    }

    public boolean isInMulti() {
        return this.isInMulti;
    }

    public boolean isInWatch() {
        return this.isInWatch;
    }


    private byte[][] joinParameters(byte[] first, byte[][] rest) {
        byte[][] result = new byte[rest.length + 1][];
        result[0] = first;
        System.arraycopy(rest, 0, result, 1, rest.length);
        return result;
    }

    private byte[][] joinParameters(byte[] first, byte[] second, byte[][] rest) {
        byte[][] result = new byte[rest.length + 2][];
        result[0] = first;
        result[1] = second;
        System.arraycopy(rest, 0, result, 2, rest.length);
        return result;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDb(int db) {
        this.db = db;
    }

    public int getDB() {
        return this.db;
    }

    public void connect(){
        if(!this.isConnected()){
            super.connect();
            if(this.user != null){
                this.auth(this.user, this.password);
                this.getStatusCodeReply();
            } else if(this.password != null){
                this.auth(this.password);
                this.getStatusCodeReply();
            }

            if(this.db > 0){
                this.select(this.db);
                this.getStatusCodeReply();
            }
        }
    }
    public void auth(String password) {
        this.setPassword(password);
        this.sendCommand(RPCProtocol.Command.AUTH, new String[]{password});
    }

    public void auth(String user, String password) {
        this.setUser(user);
        this.setPassword(password);
        this.sendCommand(RPCProtocol.Command.AUTH, new String[]{user, password});
    }

    public void select(int index) {
        this.sendCommand(RPCProtocol.Command.SELECT, new byte[][]{RPCProtocol.toByteArray(index)});
    }
    public void ping() {
        this.sendCommand(RPCProtocol.Command.PING);
    }

    public void ping(byte[] message) {
        this.sendCommand(RPCProtocol.Command.PING, new byte[][]{message});
    }

    public void set(byte[] key, byte[] value) {
        this.sendCommand(RPCProtocol.Command.SET, new byte[][]{key, value});
    }

    public void set(byte[] key, byte[] value, SetParams params) {
        this.sendCommand(RPCProtocol.Command.SET, params.getByteParams(new byte[][]{key, value}));
    }

    public void get(byte[] key) {
        this.sendCommand(RPCProtocol.Command.GET, new byte[][]{key});
    }

    public void rpc(byte[] key, byte[] value){
        this.sendCommand(RPCProtocol.Command.RPC, new byte[][]{key, value});
    }

    public void rpc(String... args){
        this.sendCommand(RPCProtocol.Command.RPC, args);
    }


    public void quit() {
        this.db = 0;
        this.sendCommand(RPCProtocol.Command.QUIT);
    }
}
