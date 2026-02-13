package com.bhcode.rpc.consumer;

import com.bhcode.rpc.api.Add;
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
public class Consumer implements Add {

    @Override
    public int add(int a, int b) {

        try {

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
                                            addResultFuture.complete(Integer.valueOf(response.getResult().toString()));
                                        }
                                    });
                        }
                    });

            ChannelFuture channelFuture = bootStrap.connect("127.0.0.1", 8888).sync();
            Request request = new Request();
            request.setMethodName("add");
            request.setParams(new Object[]{a, b});
            request.setParamTypes(new Class[]{int.class, int.class});
            request.setServiceName(Add.class.getName());
            channelFuture.channel().writeAndFlush(request);
            return addResultFuture.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
