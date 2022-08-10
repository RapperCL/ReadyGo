package com.cy.readygo.nacos.invoker;


import com.cy.readygo.core.invocation.Invocation;

public interface RpcInvoker {
    Object invoke(Invocation invocation);
}
