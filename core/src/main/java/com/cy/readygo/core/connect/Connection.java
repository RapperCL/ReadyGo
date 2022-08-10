package com.cy.readygo.core.connect;


import com.cy.readygo.core.RPCCommand.RPCProtocol;
import com.cy.readygo.core.RPCCommand.RPCProtocolCommand;
import com.cy.readygo.core.encode.SafeEncoder;
import com.cy.readygo.core.exception.RedisXConnectionException;
import com.cy.readygo.core.exception.RedisXDataException;
import com.cy.readygo.core.socket.DefaultRedisXSocketFactory;
import com.cy.readygo.core.socket.RedisXSocketFactory;
import com.cy.readygo.core.stream.RedisInputStream;
import com.cy.readygo.core.stream.RedisOutputStream;
import com.cy.readygo.core.utils.IOUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Connection implements Closeable {

    private static final byte[][] EMPTY_ARGS = new byte[0][];

    private RedisXSocketFactory redisXSocketFactory;

    private Socket socket;

    private RedisOutputStream redisOutputStream;

    private RedisInputStream redisInputStream;

    private boolean broken;

    public Connection(){
        this("localhost");
    }

    public Connection(String host){
        this(host, 6379);
    }

    public Connection(String host, int port){
        this(host,port, false);
    }

    public Connection(String host, int port, boolean ssl) {
        this(host, port, ssl, (SSLSocketFactory)null, (SSLParameters)null, (HostnameVerifier)null);
    }

    public Connection(String host, int port, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        this((RedisXSocketFactory)(new DefaultRedisXSocketFactory(host, port, 2000, 2000, ssl, sslSocketFactory, sslParameters, hostnameVerifier)));
    }

    public Connection(RedisXSocketFactory jedisSocketFactory) {
        this.broken = false;
        this.redisXSocketFactory = jedisSocketFactory;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public int getConnectionTimeout() {
        return this.redisXSocketFactory.getConnectionTimeout();
    }

    public int getSoTimeout() {
        return this.redisXSocketFactory.getSoTimeout();
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.redisXSocketFactory.setConnectionTimeout(connectionTimeout);
    }

    public void setSoTimeout(int soTimeout) {
        this.redisXSocketFactory.setSoTimeout(soTimeout);
    }

    /**
     * 判断连接是否正常
     * @return
     */
    public boolean isConnected() {
        return this.socket != null && this.socket.isBound() && !this.socket.isClosed() && this.socket.isConnected() && !this.socket.isInputShutdown() && !this.socket.isOutputShutdown();
    }

    public void setTimeoutInfinite(){
        try{
            if(!this.isConnected()){
                this.connect();
            }
            this.socket.setSoTimeout(0);
        } catch (IOException var2){
            this.broken = true;
            throw new RedisXConnectionException("Failed connecting to " + this.redisXSocketFactory.getDescription(), var2);
        }
    }

    /**
     * 连接
     */
    public void connect() {
        if (!this.isConnected()) {
            try {
                this.socket = this.redisXSocketFactory.createSocket();
                this.redisOutputStream = new RedisOutputStream(this.socket.getOutputStream());
                this.redisInputStream = new RedisInputStream(this.socket.getInputStream());
            } catch (IOException var2) {
                this.broken = true;
                throw new RedisXConnectionException("Failed connecting to " + this.redisXSocketFactory.getDescription(), var2);
            }
        }
    }

    /**
     * 关闭
     */
    public void close() {
        this.disconnect();
    }

    /**
     * 断开连接
     * 1 断开连接之前，确保发送缓存区数据全部已经发送
     * 2 关闭socket
     */
    public void disconnect() {
        if (this.isConnected()) {
            try {
                this.redisOutputStream.flush();
                this.socket.close();
            } catch (IOException var5) {
                this.broken = true;
                throw new RedisXConnectionException(var5);
            } finally {
                // 确保socket正常关闭
               IOUtils.closeQuietly(this.socket);
            }
        }
    }

    public String getStatusCodeReply() {
        this.flush();
        byte[] resp = (byte[])((byte[])this.readProtocolWithCheckingBroken());
        return null == resp ? null : SafeEncoder.encode(resp);
    }

    public String getBulkReply() {
        byte[] result = this.getBinaryBulkReply();
        return null != result ? SafeEncoder.encode(result) : null;
    }

    public byte[] getBinaryBulkReply() {
        this.flush();
        return (byte[])((byte[])this.readProtocolWithCheckingBroken());
    }

    public Long getIntegerReply() {
        this.flush();
        return (Long)this.readProtocolWithCheckingBroken();
    }

    public List<String> getMultiBulkReply() {
         return build(this.getBinaryMultiBulkReply());
    }

    public List<byte[]> getBinaryMultiBulkReply() {
        this.flush();
        return (List)this.readProtocolWithCheckingBroken();
    }


        public List<String> build(Object data) {
            if (null == data) {
                return null;
            } else {
                List<byte[]> l = (List) data;
                ArrayList<String> result = new ArrayList(l.size());
                Iterator var4 = l.iterator();

                while (var4.hasNext()) {
                    byte[] barray = (byte[]) var4.next();
                    if (barray == null) {
                        result.add(null);
                    } else {
                        result.add(SafeEncoder.encode(barray));
                    }
                }
                return result;
            }
        }


    @Deprecated
    public List<Object> getRawObjectMultiBulkReply() {
        return this.getUnflushedObjectMultiBulkReply();
    }

    public List<Object> getUnflushedObjectMultiBulkReply() {
        return (List)this.readProtocolWithCheckingBroken();
    }

    public List<Object> getObjectMultiBulkReply() {
        this.flush();
        return this.getUnflushedObjectMultiBulkReply();
    }

    public List<Long> getIntegerMultiBulkReply() {
        this.flush();
        return (List)this.readProtocolWithCheckingBroken();
    }

    public Object getOne() {
        this.flush();
        return this.readProtocolWithCheckingBroken();
    }

    public boolean isBroken() {
        return this.broken;
    }



    protected Object readProtocolWithCheckingBroken() {
        if (this.broken) {
            throw new RedisXConnectionException("Attempting to read from a broken connection");
        } else {
            try {
                return RPCProtocol.read(this.redisInputStream);
            } catch (RedisXConnectionException var2) {
                this.broken = true;
                throw var2;
            }
        }
    }

    public List<Object> getMany(int count) {
        this.flush();
        List<Object> responses = new ArrayList(count);

        for(int i = 0; i < count; ++i) {
            try {
                responses.add(this.readProtocolWithCheckingBroken());
            } catch (RedisXDataException var5) {
                responses.add(var5);
            }
        }

        return responses;
    }

    /**
     * 刷新输出流，立即写出
     */
    protected void flush() {
        try {
            this.redisOutputStream.flush();
        } catch (IOException var2) {
            this.broken = true;
            throw new RedisXConnectionException(var2);
        }
    }

    public void rollbackTimeOut(){
        try{
            this.socket.setSoTimeout(this.redisXSocketFactory.getSoTimeout());
        } catch (SocketException var2){
            this.broken = true;
            throw  new RedisXConnectionException(var2);
        }
    }

    public void sendCommand(RPCProtocolCommand cmd) {
        this.sendCommand(cmd, EMPTY_ARGS);
    }

    public void sendCommand(RPCProtocolCommand command, String... args){
        byte[][] bargs = new byte[args.length][];

        for(int i = 0; i < args.length ; ++i){
            bargs[i] = SafeEncoder.encode(args[i]);
        }

        this.sendCommand(command, bargs);
    }

    /**
     * 发送请求
     * @param command
     * @param args
     */
    public void sendCommand(RPCProtocolCommand command, byte[]... args){
        try{
            // 确保连接正常
            this.connect();
            // 调用协议层对发送的数据编码
            RPCProtocol.sendCommand(this.redisOutputStream, command, args);
        } catch (RedisXConnectionException var4){
            this.broken = true;
            throw var4;
        }
    }

    public String getHost(){
        return this.redisXSocketFactory.getHost();
    }

    public void setHost(String host){
        this.redisXSocketFactory.setHost(host);
    }

    public int getPort() {
        return this.redisXSocketFactory.getPort();
    }

    public void setPort(int port) {
        this.redisXSocketFactory.setPort(port);
    }



}
