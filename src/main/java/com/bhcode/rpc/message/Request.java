package com.bhcode.rpc.message;

import lombok.Data;

/**
 * @author hqf0330@gmail.com
 */
@Data
public class Request {
    private String serviceName;
    private String methodName;
    private String[] paramTypes;
    private Object[] params;
}
