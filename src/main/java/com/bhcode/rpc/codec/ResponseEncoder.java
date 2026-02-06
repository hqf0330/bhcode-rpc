package com.bhcode.rpc.codec;

import com.alibaba.fastjson2.JSONObject;
import com.bhcode.rpc.message.Message;
import com.bhcode.rpc.message.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

/**
 * @author hqf0330@gmail.com
 */
public class ResponseEncoder extends MessageToByteEncoder<Response> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Response response, ByteBuf out) throws Exception {
        byte[] logic = Message.LOGIC;
        byte messageType = Message.MessageType.RESPONSE.getCode();
        byte[] body = serializeResponse(response);
        int length = logic.length + Byte.BYTES + body.length;
        out.writeInt(length);
        out.writeBytes(logic);
        out.writeByte(messageType);
        out.writeBytes(body);
    }

    private byte[] serializeResponse(Response response) {
        return JSONObject.toJSONString(response).getBytes(StandardCharsets.UTF_8);
    }
}
