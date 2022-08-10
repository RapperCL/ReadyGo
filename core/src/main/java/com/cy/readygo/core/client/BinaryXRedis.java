package com.cy.readygo.core.client;



import com.cy.readygo.core.RPCCommand.BinaryXRedisCommands;
import com.cy.readygo.core.encode.SafeEncoder;
import com.cy.readygo.core.exception.InvalidURIException;
import com.cy.readygo.core.invocation.Invocation;
import com.cy.readygo.core.utils.RedisXURIHelper;
import com.cy.readygo.core.utils.SetParams;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;

public class BinaryXRedis  implements BinaryXRedisCommands {
    protected  Client client;
    private final byte[][] dummyArray;

    public BinaryXRedis(){
        this.client = null;
        this.dummyArray = new byte[0][];
    }

    public BinaryXRedis(String host){
        this.client = null;
        this.dummyArray = new byte[0][];
        URI uri = URI.create(host);
        if(RedisXURIHelper.isValid(uri)){
            this.initializeClientFromURI(uri);
        } else{
            this.client = new Client(host);
        }
    }

    public BinaryXRedis(String host, int port){
        this.client = null;
        this.dummyArray = new byte[0][];
        this.client = new Client(host, port);
    }

    public BinaryXRedis(String host, int port , boolean ssl){
        this.client = null;
        this.dummyArray = new byte[0][];
        this.client = new Client(host, port, ssl);
    }

    public BinaryXRedis(String host, int port, int timeout){
        this(host,port, timeout,timeout);
    }

    public BinaryXRedis(String host, int port, int connectionTimeout, int soTimeout){
        this.client = null;
        this.dummyArray = new byte[0][];
        this.client = new Client(host, port);
        this.client.setConnectionTimeout(connectionTimeout);
        this.client.setSoTimeout(soTimeout);
    }

    public BinaryXRedis(String host, int port, int connectionTimeout, boolean ssl){
        this.client = null;
        this.dummyArray = new byte[0][];
        this.client = new Client(host, port, ssl);
        this.client.setConnectionTimeout(connectionTimeout);

    }
    private void initializeClientFromURI(URI uri) {
        this.initializeClientFromURI(uri, (SSLSocketFactory)null, (SSLParameters)null, (HostnameVerifier)null);
    }

    private void initializeClientFromURI(URI uri, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        if (!RedisXURIHelper.isValid(uri)) {
            throw new InvalidURIException(String.format("Cannot open Redis connection due invalid URI. %s", uri.toString()));
        } else {
            this.client = new Client(uri.getHost(), uri.getPort(), RedisXURIHelper.isRedisSSLScheme(uri), sslSocketFactory, sslParameters, hostnameVerifier);
            String password = RedisXURIHelper.getPassword(uri);
            if (password != null) {
                String user = RedisXURIHelper.getUser(uri);
                if (user == null) {
                    this.client.auth(password);
                } else {
                    this.client.auth(user, password);
                }

                this.client.getStatusCodeReply();
            }

            int dbIndex = RedisXURIHelper.getDBIndex(uri);
            if (dbIndex > 0) {
                this.client.select(dbIndex);
                this.client.getStatusCodeReply();
                this.client.setDb(dbIndex);
            }

        }
    }

    @Override
    public String set(byte[] var1, byte[] var2) {
        this.client.set(var1, var2);
        return this.client.getStatusCodeReply();
    }

    public String set(String key, String  value){
        return set(SafeEncoder.encode(key), SafeEncoder.encode(value));
    }

    @Override
    public String set(byte[] var1, byte[] var2, SetParams var3) {
        this.client.set(var1, var2, var3);
        return this.client.getStatusCodeReply();
    }

    @Override
    public byte[] get(byte[] var1) {
        this.client.get(var1);
        return this.client.getBinaryBulkReply();
    }

    public String get(String key){
        return SafeEncoder.encode(get(SafeEncoder.encode(key)));
    }

    @Override
    public byte[] rpc(byte[] var1, byte[] var2) {
        this.client.rpc(var1, var2);
        return this.client.getBinaryBulkReply();
    }

    public String rpc(String serviceName,  String protocol,String methodName, String data, long sid){
        this.client.rpc(serviceName, protocol, methodName, data, sid+"");
        return SafeEncoder.encode(this.client.getBinaryBulkReply());
    }

    public String rpc(Invocation invocation){
        String[] strs = new String[]{invocation.getServiceName(),invocation.getProtocol(),invocation.getMethodName(),invocation.getData(),
                String.valueOf(invocation.getSid())};
        this.client.rpc(strs);
        return SafeEncoder.encode(this.client.getBinaryBulkReply());
    }

    public int getDB() {
        return this.client.getDB();
    }

    public String select(int index) {
        this.client.select(index);
        String statusCodeReply = this.client.getStatusCodeReply();
        this.client.setDb(index);
        return statusCodeReply;
    }

    public boolean isConnected() {
        return this.client.isConnected();
    }

    /**
     * 1 发送quit指令 退出
     * 2 断开socket连接
     */
    public String quit() {
        this.client.quit();
        String quitReturn = this.client.getStatusCodeReply();
        this.client.disconnect();
        return quitReturn;
    }

    public void connect() {
        this.client.connect();
    }


    public void disconnect() {
        this.client.disconnect();
    }

    public String auth(String user, String password) {

        this.client.auth(user, password);
        return this.client.getStatusCodeReply();
    }

    public String auth( String password) {

        this.client.auth(password);
        return this.client.getStatusCodeReply();
    }


    public void close() {
        this.client.close();
    }

    public Client getClient() {
        return this.client;
    }
}
