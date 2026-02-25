package com.bhcode.rpc.consumer;

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
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author hqf0330@gmail.com
 */
@Slf4j
public class ConsumerProxyFactory {

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

                                        if (requestFuture == null) {
                                            log.warn("request id {} not found", response.getRequestId());
                                            return;
                                        }
                                        requestFuture.complete(response);
                                    }
                                });
                    }
                });
    }

    public <I> I createConsumerProxy(Class<I> interfaceClass) {

        return (I) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{interfaceClass},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        if (method.getName().equals("equals")) {
                            return proxy == args[0];
                        }
                        if (method.getName().equals("toString")) {
                            return "BHCode Proxy Consumer " + interfaceClass.getName();
                        }
                        if (method.getName().equals("hashCode")) {
                            return System.identityHashCode(proxy);
                        }
                    }
                    try {
                        CompletableFuture<Response> responseFuture = new CompletableFuture<>();

                        Channel channel = connectionManager.getChannel("127.0.0.1", 8888);
                        if (channel == null) {
                            throw new RpcException("channel is null");
                        }

                        Request request = new Request();
                        request.setMethodName(method.getName());
                        request.setParams(args);
                        request.setParamTypes(method.getParameterTypes());
                        request.setServiceName(interfaceClass.getName());
                        inFlightMap.put(request.getRequestId(), responseFuture);
                        channel.writeAndFlush(request).addListener(f -> {
                            if (!f.isSuccess()) {
                                inFlightMap.remove(request.getRequestId(), responseFuture);
                            }
                        });
                        Response response = responseFuture.get(3, TimeUnit.SECONDS);

                        if (response.getCode() == 200) {
                            return response.getResult();
                        }
                        throw new RpcException(response.getErrorMessage());
                    } catch (RpcException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
