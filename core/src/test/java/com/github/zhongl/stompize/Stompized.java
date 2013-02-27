package com.github.zhongl.stompize;

import com.google.common.base.Function;
import java.util.*;


/**
 * This is a class help to programe and test ASM  bytecode manipulation.
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public class Stompized extends StompClient {

    protected Stompized(Function<String, Void> output) {
        super(output);
    }

    @Override
    public void connect(AcceptVersion acceptVersion, Host host, Header... optionals) {
        List<Header> headers = new ArrayList<Header>();

        headers.add(checkNotNull(acceptVersion, "accept-version"));
        headers.add(checkNotNull(host, "host"));

        Collections.addAll(headers, optionals);

        out("CONNECT", headers, null);
    }

    @Override
    public void connected(Version version, Header... optionals) {
        super.connected(checkNotNull(version, "version"), optionals);
    }

    @Override
    public void send(Destination destination, Text content, Header... optionals) {
        List<Header> headers = new ArrayList<Header>();
        headers.add(checkNotNull(destination, "destination"));
        out("SEND", headers, content);
    }

    @Override
    public void receipt(ReceiptId receiptId) {
        super.receipt(checkNotNull(receiptId, "receipt-id"));
    }

    @Override
    public void message(Destination destination, MessageId messageId, Subscription subscription, Text content, Header... optionals) {
        List<Header> headers = new ArrayList<Header>();

        headers.add(checkNotNull(destination, "destination"));
        headers.add(checkNotNull(messageId, "messageId"));
        headers.add(checkNotNull(subscription, "subscription"));

        super.message(destination, messageId, subscription, content, optionals);
    }

    @Override
    public void disconnect(Header... optionals) {
        List<Header> headers = new ArrayList<Header>();

        Collections.addAll(headers, optionals);

        out("DISCONNECT", headers, null);
    }

    @Override
    public void apply(String command, Map<String, String> headers, Object content) {
        if ("MESSAGE".equals(command)) {
            message(new Destination(headers.remove("destination")),
                    new MessageId(headers.remove("message-id")),
                    new Subscription(headers.remove("subscription")),
                    new Text(content.toString()),
                    toOptionals(Arrays.<Class<? extends Header>>asList(Ack.class, Receipt.class), headers));
            return;
        }

        if ("ERROR".equals(command)) {
            error(new Text(content), toOptionals(Arrays.<Class<? extends Header>>asList(Message.class, ReceiptId.class), headers));
            return;
        }
    }

}
