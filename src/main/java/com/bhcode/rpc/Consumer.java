package com.bhcode.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

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
                                .addLast(new LineBasedFrameDecoder(1024))
                                .addLast(new StringDecoder())
                                .addLast(new StringEncoder())
                                .addLast(new SimpleChannelInboundHandler<String>() {
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
                                        int result = Integer.parseInt(s);
                                        addResultFuture.complete(result);
                                        channelHandlerContext.close();
                                    }
                                });
                    }
                });

        ChannelFuture channelFuture = bootStrap.connect("127.0.0.1", 8888).sync();
        channelFuture.channel().writeAndFlush("add," + a + "," + b + "\n");
        return addResultFuture.get();
    }
}
