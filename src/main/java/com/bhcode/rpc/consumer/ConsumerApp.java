package com.bhcode.rpc.consumer;

import com.bhcode.rpc.api.Add;

/**
 * @author hqf0330@gmail.com
 */
public class ConsumerApp {
    public static void main(String[] args) throws Exception {
        Add consumer = new Consumer();
        System.out.println(consumer.add(1, 2));
        System.out.println(consumer.add(3, 4));
    }
}
