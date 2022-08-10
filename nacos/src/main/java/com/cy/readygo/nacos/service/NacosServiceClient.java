package com.cy.readygo.nacos.service;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceInstance;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;

import java.util.*;

/**
 * nacos提供的NacosDiscoveryClient 仅支持查询，不支持订阅，于是新写一个基于NamingService的操作类
 */

public class NacosServiceClient {

    private static final Logger log = LoggerFactory.getLogger(NacosServiceClient.class);

    private NacosDiscoveryProperties discoveryProperties;

    private final NamingService namingService;

    public NacosServiceClient(NacosDiscoveryProperties nacosDiscoveryProperties){
        this.discoveryProperties = nacosDiscoveryProperties;
        this.namingService = nacosDiscoveryProperties.namingServiceInstance();
    }
    // 查询实例
    public List<ServiceInstance> getInstances(String serviceId) {
        try{
            String group = this.discoveryProperties.getGroup();
            List<Instance> instances = this.namingService.selectInstances(serviceId, group, true);
            return hostToServiceInstanceList(instances, serviceId);
        } catch (Exception e){
            log.error("NacosServiceClient getInstances error", e);
            return Collections.emptyList();
        }
    }
   // 查询服务信息
    public List<String> getServices() {
        try {
            String group = this.discoveryProperties.getGroup();
            ListView<String> services = this.namingService.getServicesOfServer(1, 500, group);
            return services.getData();
        } catch (Exception var2) {
            log.error("NacosServiceClient getServices name from nacos server fail,", var2);
            return Collections.emptyList();
        }
    }

    // 订阅服务
    public void subscribe(String service, com.alibaba.nacos.api.naming.listener.EventListener listener)  {
        try{
            this.namingService.subscribe(service, listener);
        } catch (Exception e){
            log.error("NacosServiceClient subscribe error", e);
        }

    }

    // 注册服务
    public void registerInstance(String service, String ip, int port){
        try{
            this.namingService.registerInstance(service, ip, port);
        } catch (Exception e){
            log.error("NacosServiceClient registerInstance error",e);
        }

    }

    // 注销服务实例
    public void deRegisterInstance(String service, String ip, int port) {
        try{
            this.namingService.deregisterInstance(service, ip, port);
        } catch (Exception e){
            log.error("NacosServiceClient deregisterInstance error",e);
        }
    }

    public static List<ServiceInstance> hostToServiceInstanceList(List<Instance> instances, String serviceId) {
        List<ServiceInstance> result = new ArrayList(instances.size());
        Iterator var3 = instances.iterator();

        while(var3.hasNext()) {
            Instance instance = (Instance)var3.next();
            ServiceInstance serviceInstance = hostToServiceInstance(instance, serviceId);
            if (serviceInstance != null) {
                result.add(serviceInstance);
            }
        }

        return result;
    }

    public static ServiceInstance hostToServiceInstance(Instance instance, String serviceId) {
        if (instance != null && instance.isEnabled() && instance.isHealthy()) {
            NacosServiceInstance nacosServiceInstance = new NacosServiceInstance();
            nacosServiceInstance.setHost(instance.getIp());
            nacosServiceInstance.setPort(instance.getPort());
            nacosServiceInstance.setServiceId(serviceId);
            Map<String, String> metadata = new HashMap();
            metadata.put("nacos.instanceId", instance.getInstanceId());
            metadata.put("nacos.weight", instance.getWeight() + "");
            metadata.put("nacos.healthy", instance.isHealthy() + "");
            metadata.put("nacos.cluster", instance.getClusterName() + "");
            metadata.putAll(instance.getMetadata());
            nacosServiceInstance.setMetadata(metadata);
            if (metadata.containsKey("secure")) {
                boolean secure = Boolean.parseBoolean((String)metadata.get("secure"));
                nacosServiceInstance.setSecure(secure);
            }

            return nacosServiceInstance;
        } else {
            return null;
        }
    }

}
