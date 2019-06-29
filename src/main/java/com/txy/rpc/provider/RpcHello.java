package com.txy.rpc.provider;

import com.txy.rpc.api.IRpcHello;

public class RpcHello implements IRpcHello {
    @Override
    public String hello(String name) {
        return "Hello ," + name + " !";
    }
}
