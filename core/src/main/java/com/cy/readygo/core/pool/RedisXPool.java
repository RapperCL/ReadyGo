package com.cy.readygo.core.pool;
 


import com.cy.readygo.core.client.RedisX;
import com.cy.readygo.core.exception.RedisXException;
import com.cy.readygo.core.utils.RedisXURIHelper;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.net.URI;

public class RedisXPool extends RedisXPoolAbstract{

    public RedisXPool(){
        this(  "localhost", 6379);
    }

    public RedisXPool(GenericObjectPoolConfig poolConfig, String host){
        this(poolConfig, host, 6379);
    }



    public RedisXPool(String host, int port){

        this(new GenericObjectPoolConfig(), host, port);
    }

    public RedisXPool(String host){
        URI uri = URI.create(host);
        if(RedisXURIHelper.isValid(uri)){
            this.internalPool = new GenericObjectPool(new RedisXFactory(uri, 2000, 2000,
                    null), new GenericObjectPoolConfig<>());
        }else{
            this.internalPool = new GenericObjectPool<>(new RedisXFactory(host, 6379,
                    2000, 2000, null ),
                    new GenericObjectPoolConfig<>());
        }
    }

    public RedisXPool(GenericObjectPoolConfig poolConfig, String host, int port) {
        this(poolConfig, host, port,2000,2000,null);
    }

    public RedisXPool(GenericObjectPoolConfig poolConfig, String host, int port, int connectionTimeout) {
        this(poolConfig, host, port,connectionTimeout,connectionTimeout,null);
    }

    public RedisXPool(GenericObjectPoolConfig poolConfig, String host, int port, int connectionTimeout,int soTimeout) {
        this(poolConfig, host, port,connectionTimeout, soTimeout,null);
    }

    public RedisXPool(GenericObjectPoolConfig poolConfig, String host, int port, int connectionTimeout,
                      int soTimeout, String password){
         super(poolConfig, new RedisXFactory(host, port, connectionTimeout, soTimeout, password));
    }

    public RedisX getResource(){
        RedisX redisX = super.getResource();
        redisX.setDataSource(this);
        return redisX;
    }

    protected void returnBrokenResource(RedisX resource) {
        if (resource != null) {
            this.returnBrokenResourceObject(resource);
        }
    }


    public void returnResource(RedisX resource) {
        if (resource != null) {
            try {
                this.returnResourceObject(resource);
            } catch (Exception var3) {
                this.returnBrokenResource(resource);
                throw new RedisXException("Resource is returned to the pool as broken", var3);
            }
        }
    }
}
