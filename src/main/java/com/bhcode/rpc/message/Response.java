package com.bhcode.rpc.message;

import lombok.Data;

/**
 * @author hqf0330@gmail.com
 */
@Data
public class Response {
    Object result;
    int code;
    String errorMessage;

    public static Response success(Object result) {
        Response response = new Response();
        response.setResult(result);
        response.setCode(200);
        return response;
    }

    public static Response fail(String errorMessage) {
        Response response = new Response();
        response.setCode(400);
        response.setErrorMessage(errorMessage);
        return response;
    }
}
