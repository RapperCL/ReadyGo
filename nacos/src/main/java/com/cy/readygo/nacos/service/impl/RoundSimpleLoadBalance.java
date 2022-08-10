package com.cy.readygo.nacos.service.impl;


import com.cy.readygo.nacos.service.LoadBalance;
import org.springframework.cloud.client.ServiceInstance;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


public class RoundSimpleLoadBalance implements LoadBalance {

    private static final ConcurrentHashMap<String, AtomicInteger> roundMap = new ConcurrentHashMap<>();

    @Override
    public ServiceInstance select(List<ServiceInstance> services) {
        String key = getUniKey(services.get(0));
        roundMap.putIfAbsent(key, new AtomicInteger(0));

        AtomicInteger roundNum = roundMap.get(key);
        if(roundNum.getAndIncrement() == Integer.MAX_VALUE){
            roundNum.set(0);
        }
        int ind = roundNum.get() % services.size();
        return services.get(ind);
    }

    private String getUniKey(ServiceInstance instance){
        return instance.getServiceId();
    }
}
