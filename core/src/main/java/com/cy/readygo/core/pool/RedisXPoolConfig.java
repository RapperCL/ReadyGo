package com.cy.readygo.core.pool;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

public class RedisXPoolConfig extends GenericObjectPoolConfig {
    public RedisXPoolConfig(){
        this.setTestWhileIdle(true);
        this.setMinEvictableIdleTimeMillis(60000L);
        this.setTimeBetweenEvictionRunsMillis(30000L);
        this.setNumTestsPerEvictionRun(-1);
    }
}
