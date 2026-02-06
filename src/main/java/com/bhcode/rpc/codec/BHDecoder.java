package com.bhcode.rpc.codec;

import com.alibaba.fastjson2.JSONObject;
import com.bhcode.rpc.message.Message;
import com.bhcode.rpc.message.Request;
import com.bhcode.rpc.message.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author hqf0330@gmail.com
 */
public class BHDecoder extends LengthFieldBasedFrameDecoder {

    public BHDecoder() {
        super(1024 * 1024, 0, Integer.BYTES, 0, Integer.BYTES);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        byte[] logic = new byte[Message.LOGIC.length];
        frame.readBytes(logic);
        if (!Arrays.equals(logic, Message.LOGIC)) {
            throw new IllegalArgumentException("Logical message is not correct");
        }
        byte messageType = frame.readByte();

        byte[] body = new byte[frame.readableBytes()];
        frame.readBytes(body);
        if (Objects.equals(Message.MessageType.REQUEST.getCode(), messageType)) {
            return deserializeRequest(body);
        }

        if (Objects.equals(Message.MessageType.RESPONSE.getCode(), messageType)) {
            return deserializeResponse(body);
        }

        throw new IllegalArgumentException("invalid message type: " + messageType);
    }

    private Request deserializeRequest(byte[] body) {
        return JSONObject.parseObject(new String(body, StandardCharsets.UTF_8), Request.class);
    }

    private Response deserializeResponse(byte[] body) {
        return JSONObject.parseObject(new String(body, StandardCharsets.UTF_8), Response.class);
    }

}
