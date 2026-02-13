package com.bhcode.rpc.provider;

import com.bhcode.rpc.api.Add;

/**
 * @author hqf0330@gmail.com
 */
public class ProviderApp {
    public static void main(String[] args) {
        ProviderServer providerServer = new ProviderServer(8888);
        providerServer.register(Add.class, new AddImpl());
        providerServer.start();
    }
}
