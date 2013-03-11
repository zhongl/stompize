package com.github.zhongl.stompize;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkState;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public abstract class Client extends Stompizeble implements StompV1_2 {

    protected Client(Channel channel, long timeoutMillis) {
        this.channel = channel;
        this.timeoutMillis = timeoutMillis;
        promises = new ConcurrentHashMap<String, ChannelPromise>();
        connected = channel.newPromise();
    }

    @Override
    public void connected(Version version, Header... optionals) {
        connected.setSuccess();
    }

    @Override
    public void receipt(ReceiptId receiptId) {
        ChannelPromise promise = promises.remove(receiptId.value());
        if (promise != null) promise.setSuccess();
    }

    @Override
    public void message(Destination destination, MessageId messageId, Subscription subscription, Text content, Header... optionals) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void error(Text content, Header... optionals) {
        ReceiptId receiptId = null;
        Message message = null;

        for (Object header : optionals) {
            if (header instanceof ReceiptId) {
                receiptId = (ReceiptId) header;
                continue;
            }

            if (header instanceof Message) {
                message = (Message) header;
                continue;
            }
        }

        StompizeException e = new StompizeException(message == null ? "NO_MESSAGE" : message.value(), content.value());

        try {
            if (!connected.isDone()) {
                connected.setFailure(e);
                return;
            }

            if (receiptId == null) throw e;

            ChannelPromise promise = promises.remove(receiptId.value());
            if (promise != null) promise.setFailure(e);
        } finally {
            close();
        }
    }

    @Override
    public void close() {
        channel.close().syncUninterruptibly();
    }

    @Override
    protected void out(String command, Iterable<Header> headers, Content<?> content) {
        if (connected.isDone()) {
            new Requesting(command, headers, content).run();
        } else {
            new Connecting(command, headers, content).run();
        }
    }

    private static boolean isConnect(String command) {return "STOMP".equals(command) || "CONNECT".equals(command);}

    private final Channel                               channel;
    private final long                                  timeoutMillis;
    private final ChannelPromise                        connected;
    private final ConcurrentMap<String, ChannelPromise> promises;

    private class Connecting extends Sending {
        public Connecting(String command, Iterable<Header> headers, Content<?> content) {
            super(Client.this.channel, Client.this.timeoutMillis, command, headers, content);
        }

        @Override
        public void run() {
            checkState(isConnect(command), "Non-connecting frame allow before Server would be connected.");
            super.run();
        }

        @Override
        protected ChannelPromise promise() { return connected; }
    }

    private class Requesting extends Sending {
        public Requesting(String command, Iterable<Header> headers, Content<?> content) {
            super(Client.this.channel, Client.this.timeoutMillis, command, headers, content);
        }

        @Override
        public void run() {
            checkState(connected.isSuccess(), "Failed to connect server.");
            checkState(!isConnect(command), "Unnecessary connecting frame after Server has connected.");
            super.run();
        }

        @Override
        protected ChannelPromise promise() { return receipt == null ? null : newPromise(); }

        private ChannelPromise newPromise() {
            final ChannelPromise promise = super.channel.newPromise();
            ChannelPromise pre = promises.putIfAbsent(receipt.value(), promise);
            checkState(pre == null, "Duplicated receipt: %s.", receipt.value());
            return promise;
        }

        @Override
        protected void write(Header header) {
            if (header instanceof Receipt) receipt = (Receipt) header;
            super.write(header);
        }

        private Receipt receipt;
    }


}
