package com.bhcode.rpc.provider;

import com.bhcode.rpc.api.Add;

/**
 * @author hqf0330@gmail.com
 */
public class AddImpl implements Add {

    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
