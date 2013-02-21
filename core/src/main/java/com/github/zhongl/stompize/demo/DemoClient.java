package com.github.zhongl.stompize.demo;

import com.github.zhongl.stompize.Content;
import com.github.zhongl.stompize.Optional;
import com.github.zhongl.stompize.Required;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public abstract class DemoClient implements Demo {

    protected DemoClient(Object o, Object o1) {}

    @Override
    public void receipt(@Required("receipt-id") String receiptId) {
        System.out.println("[C] < RECEIPT: " + receiptId);
    }

    @Override
    public void message(
            @Required("destination") String destination,
            @Required("message-id") String messageId,
            @Required("subscription") String subscription,
            @Optional("ack") String ack,
            Content content
    ) {
        System.out.println("[C] < MESSAGE: " + destination);
    }

    public void send(String destination, Content content) {
        send(destination, null, null, content);
    }

    public void ack(String id) {
        ack(id, null, null);
    }
}
