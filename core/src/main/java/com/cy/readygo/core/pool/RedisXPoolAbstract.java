package com.cy.readygo.core.pool;



import com.cy.readygo.core.client.Pool;
import com.cy.readygo.core.client.RedisX;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class RedisXPoolAbstract extends Pool<RedisX> {

    public RedisXPoolAbstract(){

    }

    public RedisXPoolAbstract(GenericObjectPoolConfig poolConfig, PooledObjectFactory factory){
        super(poolConfig, factory);
    }

    protected void returnBrokenresource(RedisX resource){
        super.returnBrokenResource(resource);
    }

    public void returnResource(RedisX resource){
        super.returnResource(resource);
    }
}
