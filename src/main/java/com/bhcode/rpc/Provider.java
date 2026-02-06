package com.bhcode.rpc;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author hqf0330@gmail.com
 */
public class Provider {

    public static void main(String[] args) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup(4))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new LineBasedFrameDecoder(1024))
                                .addLast(new StringDecoder())
                                .addLast(new StringEncoder())
                                .addLast(new SimpleChannelInboundHandler<String>() {
                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
                                        String[] splits = msg.split(",");
                                        String method = splits[0];
                                        int a = Integer.parseInt(splits[1]);
                                        int b = Integer.parseInt(splits[2]);
                                        if ("add".equals(method)) {
                                            int result = add(a, b);
                                            ctx.writeAndFlush(result + "\n");
                                        }
                                    }
                                });

                    }
                })
        ;

        ChannelFuture bindFuture = serverBootstrap.bind(8888);
        bindFuture.addListener(f -> {
            if (f.isSuccess()) {
                System.out.println("server alive! port: 8888");
            } else {
                System.out.println("server alive failed!");
            }
        });
    }

    private static int add(int a, int b) {
        return a + b;
    }
}
