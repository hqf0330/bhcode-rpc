package com.bhcode.rpc.consumer;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hqf0330@gmail.com
 */

@Slf4j
public class ConnectionManager {

    private final Map<String, ChannelWrapper> channelTable = new ConcurrentHashMap<>();

    private final Bootstrap bootstrap;

    public ConnectionManager(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public Channel getChannel(String host, int port) {
        String key = host + ":" + port;
        ChannelWrapper channelWrapper = channelTable.computeIfAbsent(key, k -> {
            try {
                ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
                Channel channel = channelFuture.channel();
                channel.closeFuture().addListener(f -> channelTable.remove(key));
                return new ChannelWrapper(channel);
            } catch (InterruptedException e) {
                log.error("connect timeout: {} {}", host, port, e);
                return new ChannelWrapper(null);
            }
        });
        Channel channel = channelWrapper.channel;
        if (channel == null || !channel.isActive()) {
            channelTable.remove(key);
        }
        return channel;
    }

    private record ChannelWrapper(Channel channel) {
    }

}
