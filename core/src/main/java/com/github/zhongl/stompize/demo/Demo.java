package com.github.zhongl.stompize.demo;

import com.github.zhongl.stompize.Content;
import com.github.zhongl.stompize.Optional;
import com.github.zhongl.stompize.Required;
import com.github.zhongl.stompize.Specification;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public interface Demo extends Specification {

    void send(
            @Required("destination") String destination,
            @Optional("transaction") String transaction,
            @Optional("receipt") String receipt,
            Content content
    );

    void receipt(
            @Required("receipt-id") String receiptId
    );

    void ack(
            @Required("id") String id,
            @Optional("receipt") String receipt,
            @Optional("transaction") String transaction
    );

    void message(
            @Required("destination") String destination,
            @Required("message-id") String messageId,
            @Required("subscription") String subscription,
            @Optional("ack") String ack,
            Content content
    );

}
