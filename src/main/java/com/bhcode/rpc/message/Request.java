package com.bhcode.rpc.message;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hqf0330@gmail.com
 */
@Data
public class Request {

    private static final AtomicInteger REQUEST_COUNTER = new AtomicInteger();

    private int requestId = REQUEST_COUNTER.getAndIncrement();

    private String serviceName;
    private String methodName;
    private Class<?>[] paramTypes;
    private Object[] params;
}
