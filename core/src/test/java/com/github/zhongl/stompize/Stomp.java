package com.github.zhongl.stompize;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public interface Stomp {
    void send(
            @Required("destination") String destination,
            @Optional("receipt") String receipt,
            Object content
    );

    void receipt(@Optional("receipt-id") String id);
}
