package com.bhcode.rpc.provider;

import com.bhcode.rpc.codec.BHDecoder;
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
import lombok.extern.slf4j.Slf4j;

/**
 * @author hqf0330@gmail.com
 */
@Slf4j
public class ProviderServer {

    private final int port;

    private EventLoopGroup bossEventLoopGroup;

    private EventLoopGroup workerEventLoopGroup;

    private final ProviderRegistry registry;

    public ProviderServer(int port) {
        this.port = port;
        this.registry = new ProviderRegistry();
    }

    public <I> void register(Class<I> interfaceClass, I serviceInstance) {
        registry.register(interfaceClass, serviceInstance);
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
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new BHDecoder())
                                    .addLast(new ResponseEncoder())
                                    .addLast(new ProviderHandler());

                        }
                    })
            ;

            serverBootstrap.bind(port).sync();
        } catch (Exception e) {
            throw new RuntimeException("ProviderServer Start Failed!", e);
        }
    }

    public class ProviderHandler extends SimpleChannelInboundHandler<Request> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
            ProviderRegistry.Invocation<?> invocation = registry.findService(request.getServiceName());
            if (invocation == null) {
                Response failResp = Response.fail(request.getRequestId(), String.format("Service %s not found!", request.getServiceName()));
                ctx.writeAndFlush(failResp);
                return;
            }
            try {
                Object result = invocation.invoke(request.getMethodName(), request.getParamTypes(),
                        request.getParams());
                log.info("serviceName: {}, method: {}, result: {}", request.getServiceName(), request.getMethodName(), result);
                ctx.writeAndFlush(Response.success(request.getRequestId(), result));
            } catch (Exception e) {
                Response failResp = Response.fail(request.getRequestId(), e.getMessage());
                ctx.writeAndFlush(failResp);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("address: {} connected", ctx.channel().remoteAddress());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info("address: {} disconnected", ctx.channel().remoteAddress());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("Exception caught", cause);
            ctx.channel().close();
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
}
