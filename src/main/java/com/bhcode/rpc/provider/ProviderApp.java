package com.bhcode.rpc.provider;

/**
 * @author hqf0330@gmail.com
 */
public class ProviderApp {
    public static void main(String[] args) {
        ProviderServer providerServer = new ProviderServer(8888);
        providerServer.start();
    }
}
