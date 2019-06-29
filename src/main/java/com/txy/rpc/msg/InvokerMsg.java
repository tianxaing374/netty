package com.txy.rpc.msg;

import lombok.Data;

import java.io.Serializable;

@Data
public class InvokerMsg implements Serializable {

    //服务名称
    private String className;
    //方法
    private String methodName;
    private Class<?>[] params;
    private Object[] values;

}
