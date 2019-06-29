package com.txy.rpc.consumer;

import com.txy.rpc.api.IRpcCalc;
import com.txy.rpc.api.IRpcHello;
import com.txy.rpc.consumer.proxy.RpcProxy;
import com.txy.rpc.registry.RpcRegistry;

public class RpcConsumer {

    public static void main(String[] args) {
        IRpcHello rpcHello = RpcProxy.create(IRpcHello.class);
        System.out.println(rpcHello.hello("txy"));

        IRpcCalc calc = RpcProxy.create(IRpcCalc.class);
        System.out.println(calc.add(1, 2));
        System.out.println(calc.sub(8, 2));
        System.out.println(calc.multi(3, 2));
        System.out.println(calc.div(6, 2));
    }

}
