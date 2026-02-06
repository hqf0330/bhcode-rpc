package com.bhcode.rpc.message;

import lombok.Data;

import java.nio.charset.StandardCharsets;

/**
 * @author hqf0330@gmail.com
 */
@Data
public class Message {
    public static final byte[] LOGIC = "hbcode".getBytes(StandardCharsets.UTF_8);

    private byte[] logic;

    private byte messageType;

    private byte[] body;

    public enum MessageType {
        REQUEST(1), RESPONSE(2);

        private final byte code;

        MessageType(int code) {
            this.code = (byte) code;
        }

        public byte getCode() {
            return code;
        }

    }
}
