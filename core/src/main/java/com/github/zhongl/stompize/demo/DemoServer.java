package com.github.zhongl.stompize.demo;

import com.github.zhongl.stompize.Content;
import com.github.zhongl.stompize.Optional;
import com.github.zhongl.stompize.Required;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public abstract class DemoServer implements Demo {
    @Override
    public void send(
            @Required("destination") String destination,
            @Optional("transaction") String transaction,
            @Optional("receipt") String receipt,
            Content content
    ) {
        System.out.println(String.format("[S] < SEND: %s, %s, %s", destination, transaction, receipt));
        receipt(receipt);
    }
}
