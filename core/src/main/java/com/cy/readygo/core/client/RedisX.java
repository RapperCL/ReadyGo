package com.cy.readygo.core.client;


import com.cy.readygo.core.RPCCommand.RPCProtocolCommand;
import com.cy.readygo.core.pool.RedisXPoolAbstract;

public class RedisX extends BinaryXRedis{
    protected RedisXPoolAbstract dataSource = null;


    public RedisX(){

    }

    public RedisX(String host) {
        super(host);
    }

    public RedisX(String host, int port) {
        super(host, port);
    }

    public RedisX(String host, int port, boolean ssl) {
        super(host, port, ssl);
    }


    public RedisX(String host, int port, int timeout) {
        super(host, port, timeout);
    }

    public RedisX(String host, int port, int timeout, boolean ssl) {
        super(host, port, timeout, ssl);
    }


    public RedisX(String host, int port, int connectionTimeout, int soTimeout) {
        super(host, port, connectionTimeout, soTimeout);
    }

    /**
     * 需要我们手动去释放 连接
     * 归还对象，归还了对象，但是连接还是保持着
     *
     * 如果没有通过池管理，那么就会直接将连接关闭掉
     */
    public void close() {
        if (this.dataSource != null) {
            RedisXPoolAbstract pool = this.dataSource;
            this.dataSource = null;
            if (this.client.isBroken()) {
                pool.returnBrokenResource(this);
            } else {
                pool.returnResource(this);
            }
        } else {
            super.close();
        }
    }

    public void setDataSource(RedisXPoolAbstract jedisPool) {
        this.dataSource = jedisPool;
    }


    public Object sendCommand(RPCProtocolCommand command, String ...args){
        this.client.sendCommand(command, args);
        return this.client.getOne();
    }
}
