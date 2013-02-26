package com.github.zhongl.stompize;

import java.util.Map;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class Invoker {

    public Invoker(ASpec aSpec) {this.aSpec = aSpec;}

    public void receive(String command, Map<String, Object> headers, Object content) {
        if ("RECEIPT".equals(command)) {
            aSpec.receipt(headers.get("receipt-id").toString());
        }
    }

    private final ASpec aSpec;
}
