package com.bhcode.rpc.provider;

import com.bhcode.rpc.codec.BHDecoder;
import com.bhcode.rpc.codec.RequestEncoder;
import com.bhcode.rpc.codec.ResponseEncoder;
import com.bhcode.rpc.message.Request;
import com.bhcode.rpc.message.Response;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author hqf0330@gmail.com
 */
public class ProviderServer {

    private final int port;

    private EventLoopGroup bossEventLoopGroup;

    private EventLoopGroup workerEventLoopGroup;

    public ProviderServer(int port) {
        this.port = port;
    }

    public void start() {
        try {

            bossEventLoopGroup = new NioEventLoopGroup();
            workerEventLoopGroup = new NioEventLoopGroup(4);

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossEventLoopGroup, workerEventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new BHDecoder())
                                    .addLast(new ResponseEncoder())
                                    .addLast(new SimpleChannelInboundHandler<Request>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
                                            System.out.println(request);
                                            Response response = new Response();
                                            response.setResult(1);
                                            ctx.writeAndFlush(response);
                                        }
                                    });

                        }
                    })
            ;

            serverBootstrap.bind(port).sync();
        } catch (Exception e) {
            throw new RuntimeException("ProviderServer Start Failed!", e);
        }
    }

    public void stop() {
        if (bossEventLoopGroup != null) {
            bossEventLoopGroup.shutdownGracefully();
        }

        if (workerEventLoopGroup != null) {
            workerEventLoopGroup.shutdownGracefully();
        }
    }

    private static int add(int a, int b) {
        return a + b;
    }
}
