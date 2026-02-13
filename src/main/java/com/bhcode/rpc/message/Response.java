package com.bhcode.rpc.message;

import lombok.Data;

/**
 * @author hqf0330@gmail.com
 */
@Data
public class Response {
    private int requestId;
    private Object result;
    private int code;
    private String errorMessage;

    public static Response success(int requestId, Object result) {
        Response response = new Response();
        response.setResult(result);
        response.setCode(200);
        response.setRequestId(requestId);
        return response;
    }

    public static Response fail(int requestId, String errorMessage) {
        Response response = new Response();
        response.setCode(400);
        response.setErrorMessage(errorMessage);
        response.setRequestId(requestId);
        return response;
    }
}
