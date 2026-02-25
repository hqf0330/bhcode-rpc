package com.bhcode.rpc.consumer;

import com.bhcode.rpc.api.Add;

/**
 * @author hqf0330@gmail.com
 */
public class ConsumerApp {
    public static void main(String[] args) throws Exception {
        ConsumerProxyFactory consumerProxyFactory = new ConsumerProxyFactory();
        Add consumer = consumerProxyFactory.createConsumerProxy(Add.class);
        System.out.println(consumer.add(1, 2));
        System.out.println(consumer.add(3, 4));
    }
}
