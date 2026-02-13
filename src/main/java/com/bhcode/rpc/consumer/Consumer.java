package com.bhcode.rpc.consumer;

import com.bhcode.rpc.api.Add;
import com.bhcode.rpc.codec.BHDecoder;
import com.bhcode.rpc.codec.RequestEncoder;
import com.bhcode.rpc.exception.RpcException;
import com.bhcode.rpc.message.Request;
import com.bhcode.rpc.message.Response;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author hqf0330@gmail.com
 */
public class Consumer implements Add {

    private final Map<Integer, CompletableFuture<?>> inFlightMap = new ConcurrentHashMap<>();

    private final ConnectionManager connectionManager = new ConnectionManager(createBootstrap());

    private Bootstrap createBootstrap() {
        return new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new BHDecoder())
                                .addLast(new RequestEncoder())
                                .addLast(new SimpleChannelInboundHandler<Response>() {
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                                                Response response) {
                                        CompletableFuture requestFuture =
                                                inFlightMap.remove(response.getRequestId());
                                        if (response.getCode() == 200) {
                                            requestFuture.complete(Integer.valueOf(response.getResult().toString()));
                                        } else {
                                            requestFuture.completeExceptionally(new RpcException(response.getErrorMessage()));
                                        }
                                    }
                                });
                    }
                });
    }

    @Override
    public int add(int a, int b) {

        try {

            CompletableFuture<Integer> addResultFuture = new CompletableFuture<>();

            Channel channel = connectionManager.getChannel("127.0.0.1", 8888);
            if (channel == null) {
                throw new RpcException("channel is null");
            }

            Request request = new Request();
            request.setMethodName("add");
            request.setParams(new Object[]{a, b});
            request.setParamTypes(new Class[]{int.class, int.class});
            request.setServiceName(Add.class.getName());
            channel.writeAndFlush(request).addListener(f -> {
                if (f.isSuccess()) {
                    inFlightMap.put(request.getRequestId(), addResultFuture);
                }
            });
            return addResultFuture.get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
