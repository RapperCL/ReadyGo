package com.cy.readygo.nacos.pool;

import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;

import com.cy.readygo.core.client.RedisX;
import com.cy.readygo.core.pool.RedisXPool;
import com.cy.readygo.core.pool.RedisXPoolConfig;
import com.cy.readygo.nacos.invoker.DefaultRpcInvoker;
import com.cy.readygo.nacos.service.NacosServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 管理redisXpool连接池
 * 1 负责根据服务名创建连接池；
 * 2 订阅服务名的变更，并同步关闭对应的连接池
 */

public class RedisXPoolHolder {

   private static final Logger log = LoggerFactory.getLogger(DefaultRpcInvoker.class);

   private NacosServiceClient serviceClient;

   // 监听到服务变更之后，进一步判断当前map中的实例与当前服务可用的实例进行比较之后，再关闭连接池
   private final static ConcurrentHashMap<String, HashMap<String, RedisXPool>> serviceMap = new ConcurrentHashMap<>();

   private final static int connection_timeout = 2000;

   private final static String SPLIT_STR = "#";

   public RedisXPoolHolder(NacosServiceClient serviceClient){
       this.serviceClient = serviceClient;
   }

   // 获取redisx连接
   public RedisX getRedisClient(ServiceInstance instance){
         String serviceKey = instance.getServiceId();
         String poolKey = getPoolKey(instance);
         HashMap<String, RedisXPool> poolMap = serviceMap.computeIfAbsent(serviceKey, key ->{
             // 创建配置放入集合
             subscribe(serviceKey, poolKey);
             return new HashMap<>();
         });

         RedisXPool redisXPool = poolMap.get(poolKey);
         if(redisXPool == null){
             synchronized (poolMap){
                 if(redisXPool == null){
                     redisXPool = new RedisXPool(getDefaultConfig(), instance.getHost(), instance.getPort(),
                             connection_timeout, connection_timeout);
                     poolMap.put(poolKey, redisXPool);
                 }
             }
         }
         return redisXPool.getResource();
   }

    private String getPoolKey(ServiceInstance instance){
       return instance.getHost() + SPLIT_STR + instance.getPort();
    }

    private String getUniqueKey(ServiceInstance instance){
        return instance.getServiceId() + SPLIT_STR + instance.getHost() + SPLIT_STR + instance.getPort();
    }


    //对建立了连接的服务，进行监听
    private void subscribe(String serviceKey, String poolkey){
         serviceClient.subscribe(serviceKey, new EventListener() {
             @Override
             public void onEvent(Event event) {
                 // 监听服务实例变更之后，根据是否健康
                 NamingEvent namingEvent = ((NamingEvent) event);
                 String key = namingEvent.getServiceName();
                 // 如果实例为空了，那么就关闭池子
                 HashMap<String, RedisXPool> poolMap = serviceMap.get(poolkey);
                 
                 List<String> validPoolList = getValidPoolKeys(namingEvent.getInstances());

                 for(Map.Entry<String, RedisXPool> item : poolMap.entrySet()){
                     if(!validPoolList.contains(item.getKey())){
                         poolMap.remove(item.getKey());
                         // 关闭池子中空闲的连接
                         try{
                             item.getValue().close();
                         } catch (Exception e){
                             log.error("RedisXPoolHolder subscribe onEvent error",e);
                         }
                     }
                 }
             }
         });
    }



    private List<String> getValidPoolKeys(List<Instance> list){
       return list.stream().map(s -> s.getIp() + SPLIT_STR + s.getPort()).collect(Collectors.toList());
    }

    private boolean validPool(String poolKey, List<Instance> list){
         for(Instance s: list){
             String sKey = s.getIp() + SPLIT_STR + s.getPort();
             if(sKey.equals(poolKey)){
                 return true;
             }
         }
         return false;
    }


    private RedisXPoolConfig getDefaultConfig(){
        RedisXPoolConfig config = new RedisXPoolConfig();
        config.setMaxIdle(5);
        config.setMaxTotal(10);
        config.setTestOnBorrow(true);
        config.setTestWhileIdle(true);
        return config;
    }
}
