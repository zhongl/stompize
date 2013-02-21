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

}
