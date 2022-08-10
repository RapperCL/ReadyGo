package com.cy.readygo.nacos.service;

import com.alibaba.nacos.api.naming.listener.EventListener;
import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

public interface ServiceInfoManager {

    List<ServiceInstance> getInstances(String serviceId);

    List<String> getServices();

    void subscribe(String service,  EventListener listener);

    void registerInstance(String service, String ip, int port);

    void doRegisterInstance(String service, String ip, int port);

}
