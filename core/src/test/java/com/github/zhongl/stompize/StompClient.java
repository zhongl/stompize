package com.github.zhongl.stompize;

import static java.util.Arrays.asList;

import com.google.common.base.Function;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public abstract class StompClient extends Stompizeble implements Stomp {

    private final Function<String, Void> output;

    protected StompClient(Function<String, Void> output) {
        this.output = output;
    }

    @Override
    public void receipt(ReceiptId receiptId) {
        output.apply(join("RECEIPT", receiptId));
    }

    @Override
    public void message(Destination destination, MessageId messageId, Subscription subscription, Text content, Header... optionals) {
        output.apply(join("MESSAGE", destination, messageId, subscription, content, asList(optionals)));
    }

    @Override
    public void connected(Version version, Header... optionals) {
        output.apply(join("CONNECT", version, asList(optionals)));
    }

    @Override
    public void error(Text content, Header... optionals) {
        output.apply(join("ERROR", content, asList(optionals)));
    }

    @Override
    protected void out(String command, Iterable<Header> headers, Content<?> content) {
        output.apply(join(command, headers, content));
    }

    private static String join(String command, Object... args) {
        return asList(command, asList(args)).toString();
    }
}
