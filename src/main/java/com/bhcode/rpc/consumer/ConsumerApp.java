package com.bhcode.rpc.consumer;

/**
 * @author hqf0330@gmail.com
 */
public class ConsumerApp {
    public static void main(String[] args) throws Exception {
        Consumer consumer = new Consumer();
        System.out.println(consumer.add(1, 2));
    }
}
