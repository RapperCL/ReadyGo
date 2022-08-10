package com.cy.readygo.nacos.service;

import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

public interface LoadBalance {

     ServiceInstance  select(List<ServiceInstance> services);
}
