package com.cy.readygo.nacos.invoker;


import com.cy.readygo.core.client.RedisX;
import com.cy.readygo.core.invocation.Invocation;
import com.cy.readygo.nacos.pool.RedisXPoolHolder;
import com.cy.readygo.nacos.service.LoadBalance;
import com.cy.readygo.nacos.service.NacosServiceClient;
import com.cy.readygo.nacos.service.impl.RoundSimpleLoadBalance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;

import java.util.List;


public class DefaultRpcInvoker implements RpcInvoker {

    private static final Logger log = LoggerFactory.getLogger(DefaultRpcInvoker.class);

    private NacosServiceClient serviceClient;

    // 后期基于spi优化
    private final static LoadBalance loadBalance = new RoundSimpleLoadBalance();
    // redis池处理器
    private final RedisXPoolHolder redisXPoolHolder;

    public DefaultRpcInvoker(NacosServiceClient serviceClient){
        this.serviceClient = serviceClient;
        this.redisXPoolHolder = new RedisXPoolHolder(serviceClient);
    }


    @Override
    // todo 查看
    public Object invoke(Invocation invocation) {
        // 根据服务名，获取所有的实例，并从中根据负载均衡策略选择一个
        String serviceName = invocation.getServiceName();
        List<ServiceInstance> instanceList = serviceClient.getInstances(serviceName);
        // 负载均衡
        ServiceInstance instance = loadBalance.select(instanceList);
        // 获取实例对应的连接
        RedisX redisX = redisXPoolHolder.getRedisClient(instance);
        // 进行执行
        return  doInvoker(redisX, invocation);

    }

    private Object doInvoker(RedisX redisX,Invocation invocation){
        return redisX.rpc(invocation);
    }

}
