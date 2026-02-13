# bhcode-rpc

一个基于 Netty 的轻量级 RPC 框架，实现了服务注册、远程调用、连接管理等核心功能。

## 项目简介

bhcode-rpc 是一个学习型的 RPC 框架，旨在通过简洁的代码展示 RPC 框架的核心原理。项目采用自定义协议、基于 Netty 的网络通信、FastJSON 序列化等技术实现。

## 核心特性

- ✅ **自定义二进制协议** - 基于长度字段的高效分帧协议
- ✅ **服务注册与发现** - 支持接口级别的服务注册
- ✅ **反射方法调用** - 动态调用服务实现类的方法
- ✅ **连接复用管理** - ConnectionManager 实现连接池
- ✅ **在途请求映射** - 支持并发 RPC 调用
- ✅ **异常处理机制** - 完整的错误响应和异常传播
- ✅ **超时控制** - 客户端调用超时保护
- ✅ **日志记录** - 完整的调用链路日志

## 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17+ | 开发语言 |
| Netty | 4.1.128.Final | 网络通信框架 |
| FastJSON | 2.0.51 | JSON 序列化 |
| Logback | 1.5.22 | 日志框架 |
| Lombok | 1.18.42 | 简化代码 |

## 项目结构

```
bhcode-rpc/
├── src/main/java/com/bhcode/rpc/
│   ├── api/                    # 服务接口定义
│   │   └── Add.java          # 示例服务接口
│   ├── codec/                  # 编解码器
│   │   ├── BHDecoder.java    # 协议解码器
│   │   ├── RequestEncoder.java # 请求编码器
│   │   └── ResponseEncoder.java # 响应编码器
│   ├── consumer/               # 服务消费者
│   │   ├── Consumer.java     # RPC 客户端
│   │   ├── ConsumerApp.java  # 消费者启动类
│   │   └── ConnectionManager.java # 连接管理器
│   ├── provider/               # 服务提供者
│   │   ├── ProviderServer.java # RPC 服务器
│   │   ├── ProviderApp.java  # 提供者启动类
│   │   ├── ProviderRegistry.java # 服务注册中心
│   │   └── AddImpl.java     # 服务实现类
│   ├── message/                # 消息体定义
│   │   ├── Message.java      # 消息基类
│   │   ├── Request.java      # 请求消息
│   │   └── Response.java     # 响应消息
│   └── exception/              # 异常定义
│       └── RpcException.java # RPC 异常
└── pom.xml                   # Maven 配置
```

## 协议设计

### 协议格式

```
┌──────────┬──────────┬───────┬──────────┬─────────┐
│ 总长度   │ Magic    │ Type  │ RequestID │ Body     │
│ 4 bytes  │ 6 bytes  │ 1byte │ 4 bytes   │ N bytes │
└──────────┴──────────┴───────┴──────────┴─────────┘
```

| 字段 | 长度 | 说明 |
|------|------|------|
| 总长度 | 4 字节 | 整个消息的长度（不含长度字段本身） |
| Magic | 6 字节 | 协议魔数："hbcode"，用于协议识别 |
| Type | 1 字节 | 消息类型：REQUEST(1) 或 RESPONSE(2) |
| RequestID | 4 字节 | 请求 ID，用于匹配请求和响应 |
| Body | N 字节 | 消息体（JSON 格式） |

### Request Body

```json
{
  "requestId": 1,
  "serviceName": "com.bhcode.rpc.api.Add",
  "methodName": "add",
  "paramTypes": ["int", "int"],
  "params": [1, 2]
}
```

### Response Body

```json
{
  "requestId": 1,
  "result": 3,
  "code": 200,
  "errorMessage": null
}
```

## 快速开始

### 1. 定义服务接口

```java
package com.bhcode.rpc.api;

public interface Add {
    int add(int a, int b);
}
```

### 2. 实现服务

```java
package com.bhcode.rpc.provider;

import com.bhcode.rpc.api.Add;

public class AddImpl implements Add {
    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
```

### 3. 启动服务提供者

```java
package com.bhcode.rpc.provider;

import com.bhcode.rpc.api.Add;

public class ProviderApp {
    public static void main(String[] args) {
        ProviderServer providerServer = new ProviderServer(8888);
        providerServer.register(Add.class, new AddImpl());
        providerServer.start();
    }
}
```

### 4. 实现服务消费者

```java
package com.bhcode.rpc.consumer;

import com.bhcode.rpc.api.Add;

public class Consumer implements Add {
    @Override
    public int add(int a, int b) {
        // RPC 调用逻辑已封装
        // ...
    }
}
```

### 5. 调用服务

```java
package com.bhcode.rpc.consumer;

import com.bhcode.rpc.api.Add;

public class ConsumerApp {
    public static void main(String[] args) throws Exception {
        Add consumer = new Consumer();
        System.out.println(consumer.add(1, 2));  // 输出: 3
    }
}
```

## 核心组件

### ProviderRegistry - 服务注册中心

维护服务接口名到服务实例的映射：

```java
private final Map<String, Invocation<?>> serviceInstanceMap = new ConcurrentHashMap<>();

public <I> void register(Class<I> interfaceClass, I serviceInstance) {
    // 注册服务
    serviceInstanceMap.put(interfaceClass.getName(),
                          new Invocation<>(interfaceClass, serviceInstance));
}
```

### ConnectionManager - 连接管理器

实现连接复用，避免每次调用都创建新连接：

```java
private final Map<String, ChannelWrapper> channelTable = new ConcurrentHashMap<>();

public Channel getChannel(String host, int port) {
    String key = host + ":" + port;
    return channelTable.computeIfAbsent(key, k -> {
        // 创建并缓存连接
        ChannelFuture future = bootstrap.connect(host, port).sync();
        return new ChannelWrapper(future.channel());
    }).channel();
}
```

### BHDecoder - 协议解码器

基于 `LengthFieldBasedFrameDecoder` 实现分帧和消息解析：

```java
protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
    // 1. 分帧
    ByteBuf frame = (ByteBuf) super.decode(ctx, in);

    // 2. 读取 Magic
    byte[] magic = new byte[6];
    frame.readBytes(magic);

    // 3. 读取 Type
    byte messageType = frame.readByte();

    // 4. 读取 Body
    byte[] body = new byte[frame.readableBytes()];
    frame.readBytes(body);

    // 5. 反序列化
    return deserialize(body, messageType);
}
```

## RPC 调用流程

```
┌─────────────┐                         ┌─────────────┐
│  Consumer   │                         │  Provider   │
└─────────────┘                         └─────────────┘
      │                                        │
      │  1. 创建 Request 对象                  │
      │     - serviceName: Add.class.getName() │
      │     - methodName: "add"                │
      │     - params: [1, 2]                  │
      │                                        │
      │  2. 序列化为 JSON                      │
      │     → "{...}"                         │
      │                                        │
      │  3. 编码为二进制协议                    │
      │     → [Len][Magic][Type][ID][Body]    │
      │                                        │
      ├─────────────── TCP ──────────────────>│
      │                                        │
      │                                4. BHDecoder 分帧
      │                                        │
      │                                5. 查找服务
      │                                   registry.findService()
      │                                        │
      │                                6. 反射调用
      │                                   invocation.invoke()
      │                                        │
      │                                7. 返回结果
      │                                        │
      │<─────────────── TCP ──────────────────│
      │                                        │
      │  8. 解码并反序列化                      │
      │                                        │
      │  9. 完成 Future                        │
      │     CompletableFuture.complete(3)       │
      │                                        │
      ▼                                        ▼
   返回 3
```

## 待优化项

- [ ] 修复 ConnectionManager 并发创建连接的问题
- [ ] 修复 inFlightMap 的 Future 注册时序问题
- [ ] RequestId 使用 Long 类型防止溢出
- [ ] 实现服务注册中心（如 ZooKeeper/Nacos）
- [ ] 支持多种序列化方式（Protobuf/Hessian）
- [ ] 实现负载均衡策略
- [ ] 添加服务熔断和降级
- [ ] 实现服务限流
- [ ] 添加性能监控和链路追踪

## 运行示例

### 启动服务提供者

```bash
mvn compile exec:java -Dexec.mainClass="com.bhcode.rpc.provider.ProviderApp"
```

### 启动服务消费者

```bash
mvn compile exec:java -Dexec.mainClass="com.bhcode.rpc.consumer.ConsumerApp"
```

## 许可证

MIT License

## 作者

[hqf0330](mailto:hqf0330@gmail.com)
