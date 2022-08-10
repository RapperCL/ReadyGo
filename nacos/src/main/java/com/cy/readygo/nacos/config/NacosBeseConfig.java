package com.cy.readygo.nacos.config;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.cy.readygo.nacos.invoker.DefaultRpcInvoker;
import com.cy.readygo.nacos.invoker.RpcInvoker;
import com.cy.readygo.nacos.service.NacosServiceClient;
import org.springframework.context.annotation.Bean;


public class NacosBeseConfig {

    @Bean
    public NacosServiceClient getNacosServiceClient(NacosDiscoveryProperties discoveryProperties){
        return new NacosServiceClient(discoveryProperties);
    }

    @Bean
    public RpcInvoker getRpcInvoker(NacosServiceClient serviceClient){
        return new DefaultRpcInvoker(serviceClient);
    }
}
