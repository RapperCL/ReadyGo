package com.cy.readygo.core.socket;


import com.cy.readygo.core.exception.RedisXConnectionException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class DefaultRedisXSocketFactory implements RedisXSocketFactory{
    private String host;
    private int port;
    private int connectionTimeout;
    private int soTimeout;
    private boolean ssl;
    private SSLSocketFactory sslSocketFactory;
    private SSLParameters sslParameters;
    private HostnameVerifier hostnameVerifier;

    public DefaultRedisXSocketFactory(String host, int port, int connectionTimeout, int soTimeout,
                                      boolean ssl, SSLSocketFactory sslSocketFactory,SSLParameters sslParameters,
                                      HostnameVerifier hostnameVerifier){
        this.host = host;
        this.port = port;
        this.connectionTimeout = connectionTimeout;
        this.soTimeout = soTimeout;
        this.ssl = ssl;
        this.sslSocketFactory = sslSocketFactory;
        this.sslParameters = sslParameters;
        this.hostnameVerifier = hostnameVerifier;
    }

    @Override
    public Socket createSocket() throws IOException {
        Socket socket = null;
        try{
            socket = new Socket();
            socket.setReuseAddress(true);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
            socket.setSoLinger(true, 0);
            socket.connect(new InetSocketAddress(this.getHost(), this.getPort()), this.getConnectionTimeout());
            socket.setSoTimeout(this.getSoTimeout());
            if(this.ssl){
                if(null == this.sslSocketFactory){
                    this.sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                }

                socket = this.sslSocketFactory.createSocket(socket, this.getHost(), this.getPort(), true);
                if(null != this.sslParameters){
                    ((SSLSocket)socket).setSSLParameters(this.sslParameters);
                }
                if (null != this.hostnameVerifier && !this.hostnameVerifier.verify(this.getHost(), ((SSLSocket)socket).getSession())) {
                    String message = String.format("The connection to '%s' failed ssl/tls hostname verification.", this.getHost());
                    throw new RedisXConnectionException(message);
                }
            }
            return socket;
        } catch (Exception var1){
            if(socket != null){
                socket.close();
            }
            throw var1;
        }
    }

    public String getDescription() {
        return this.host + ":" + this.port;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getConnectionTimeout() {
        return this.connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getSoTimeout() {
        return this.soTimeout;
    }

    public void setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
    }
}
