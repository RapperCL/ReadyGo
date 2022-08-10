package com.cy.readygo.core.pool;



import com.cy.readygo.core.client.BinaryXRedis;
import com.cy.readygo.core.client.RedisX;
import com.cy.readygo.core.exception.InvalidURIException;
import com.cy.readygo.core.exception.RedisXException;
import com.cy.readygo.core.utils.RedisXURIHelper;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;

class RedisXFactory implements PooledObjectFactory<RedisX> {
    private  static String host;
    private  static int  port;
    private final int connectionTimeout;
    private final int soTimeout;
    private final String user;
    private final String password;
    private final int database;
    private final String clientName;
    private final boolean ssl;
    private final SSLSocketFactory sslSocketFactory;
    private final SSLParameters sslParameters;
    private final HostnameVerifier hostnameVerifier;


    RedisXFactory(String host, int port, int connectionTimeout, int soTimeout, String password) {
        this(host, port, connectionTimeout, soTimeout, password, 0, null, false,  null,  null, null);
    }

    RedisXFactory(String host, int port, int connectionTimeout, int soTimeout, String user, String password, int database, String clientName) {
        this(host, port, connectionTimeout, soTimeout, user, password, database, clientName, false,  null,  null,  null);
    }


    RedisXFactory(URI uri, int connectionTimeout, int soTimeout, String clientName) {
        this(uri, connectionTimeout, soTimeout, clientName,  null,  null,  null);
    }


    RedisXFactory(String host, int port, int connectionTimeout, int soTimeout, String password, int database, String clientName, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        this(host, port, connectionTimeout, soTimeout,  null, password, database, clientName, ssl, sslSocketFactory, sslParameters, hostnameVerifier);
    }

    RedisXFactory(String host, int port, int connectionTimeout, int soTimeout, String user, String password, int database, String clientName, boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {
        this.host = host;
        this.port = port;
        this.connectionTimeout = connectionTimeout;
        this.soTimeout = soTimeout;
        this.user = user;
        this.password = password;
        this.database = database;
        this.clientName = clientName;
        this.ssl = ssl;
        this.sslSocketFactory = sslSocketFactory;
        this.sslParameters = sslParameters;
        this.hostnameVerifier = hostnameVerifier;
    }

    RedisXFactory(URI uri, int connectionTimeout, int soTimeout, String clientName, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters, HostnameVerifier hostnameVerifier) {

        if (!RedisXURIHelper.isValid(uri)) {
            throw new InvalidURIException(String.format("Cannot open Redis connection due invalid URI. %s", uri.toString()));
        } else {

            this.connectionTimeout = connectionTimeout;
            this.soTimeout = soTimeout;
            this.user = RedisXURIHelper.getUser(uri);
            this.password = RedisXURIHelper.getPassword(uri);
            this.database = RedisXURIHelper.getDBIndex(uri);
            this.clientName = clientName;
            this.ssl = RedisXURIHelper.isRedisSSLScheme(uri);
            this.sslSocketFactory = sslSocketFactory;
            this.sslParameters = sslParameters;
            this.hostnameVerifier = hostnameVerifier;
        }
    }


    // 激活对象
    @Override
    public void activateObject(PooledObject<RedisX> pooledRedis) throws Exception {
        BinaryXRedis redis = pooledRedis.getObject();
        // 这一步完全可以去掉
        if (redis.getDB() != this.database) {
            redis.select(this.database);
        }
    }

    // 销毁对象： 断开连接
    @Override
    public void destroyObject(PooledObject<RedisX> pooledRedis) throws Exception {
        BinaryXRedis redis = pooledRedis.getObject();
        if(redis.isConnected()){
            try{
                redis.quit();
            } finally {
                redis.disconnect();
            }
        }
    }

//    @Override
//    public void destroyObject(PooledObject<RedisX> p, DestroyMode destroyMode) throws Exception {
//        PooledObjectFactory.super.destroyObject(p, destroyMode);
//    }


    // 构建对象
    @Override
    public PooledObject<RedisX> makeObject() throws Exception {

        // 创建redisx对象并建立好连接
        RedisX redis = new RedisX(this.host, this.port, this.connectionTimeout, this.ssl);
        try {
            redis.connect();
            if (this.user != null) {
                redis.auth(this.user, this.password);
            } else if (this.password != null) {
                redis.auth(this.password);
            }

            if (this.database != 0) {
                redis.select(this.database);
            }

        } catch (RedisXException var4) {
            redis.close();
            throw var4;
        }
        return new DefaultPooledObject(redis);
    }


    public void passivateObject(PooledObject<RedisX> pooledRedis){

    }

    @Override
    public boolean validateObject(PooledObject<RedisX> pooledRedis){
        BinaryXRedis redis = pooledRedis.getObject();
        try{
            String connectHost = redis.getClient().getHost();
            int connectPort = redis.getClient().getPort();
            return this.host.equals(connectHost) && this.port == connectPort &&
                    redis.isConnected();
            // todo 添加pingpong机制
        } catch (Exception var2){
            return false;
        }


    }

}
