package com.bhcode.rpc.consumer;

import com.bhcode.rpc.codec.BHDecoder;
import com.bhcode.rpc.codec.RequestEncoder;
import com.bhcode.rpc.message.Request;
import com.bhcode.rpc.message.Response;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.CompletableFuture;

/**
 * @author hqf0330@gmail.com
 */
public class Consumer {
    public int add(int a, int b) throws Exception {

        CompletableFuture<Integer> addResultFuture = new CompletableFuture<>();

        Bootstrap bootStrap = new Bootstrap()
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
                                                                Response response) throws Exception {
                                        System.out.println(response);
                                        Integer result = Integer.valueOf(response.getResult().toString());
                                        addResultFuture.complete(result);
                                    }
                                });
                    }
                });

        ChannelFuture channelFuture = bootStrap.connect("127.0.0.1", 8888).sync();
        Request request = new Request();
        request.setMethodName("aaa");
        request.setParams(new Object[]{1, 2});
        request.setParamTypes(new String[]{"int", "int"});
        request.setServiceName("test");
        channelFuture.channel().writeAndFlush(request);
        return addResultFuture.get();
    }
}
