package com.cy.readygo.nacos.entity;



import com.cy.readygo.core.invocation.Invocation;

import java.io.Serializable;

public class RpcInvocation implements Invocation, Serializable {

    private static final long serialVersionUID = -4355285085441097045L;

    private String serviceName;

    private String methodName;

    private String data;

    private Long sid;


    public RpcInvocation(){

    }

    public RpcInvocation(String serviceName, String methodName, String data){
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.data = data;
    }

    public RpcInvocation(String serviceName, String methodName, String data, Long sid){
        this.serviceName = serviceName;
        this.methodName = methodName;
        this.data = data;
        this.sid = sid;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public String getData() {
        return data;
    }

    public long getSid(){
        if(sid == null){
            return System.currentTimeMillis();
        }
        return sid;
    }

    public void setServiceName(String serviceName){
        this.serviceName = serviceName;
    }

    public void setMethodName(String methodName){
        this.methodName = methodName;
    }

    public void setData(String data){
        this.data = data;
    }

    public void setSid(long sid){
        this.sid = sid;
    }


}
