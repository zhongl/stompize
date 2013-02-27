package com.github.zhongl.stompize;

import com.google.common.base.Throwables;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPromise;

import static com.github.zhongl.stompize.Bytes.buf;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public abstract class Sending implements Runnable {

    protected Sending(Channel channel, long timeoutMillis, String command, Iterable<Header> headers, Content<?> content) {
        this.channel = channel;
        this.timeoutMillis = timeoutMillis;
        this.content = content;
        this.command = command;
        this.headers = headers;
    }

    @Override
    public void run() {
        channel.write(buf(command));

        for (Header header : headers) write(header);

        writeContent();

        await(channel.flush(), promise());
    }

    protected void write(Header header) {channel.write(buf(header.toString()));}

    protected void writeContent() {
        if (content == null) {
            channel.write(NO_CONTENT);
        } else {
            write(new StompV1_2.ContentType(content.type()));
            write(new StompV1_2.ContentLength(content.length()));
            channel.write(buf(content.toString()));
        }

        channel.write(FRAME_DELIMITER);
    }

    protected abstract ChannelPromise promise();

    private void await(ChannelFuture future, final ChannelPromise promise) {

        if (promise == null) {
            if (!future.awaitUninterruptibly(timeoutMillis)) throw new StompizeException(CHANNEL_WRITE_TIMEOUT);
            if (!future.isSuccess()) throw Throwables.propagate(future.cause());
            return;
        }

        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) promise.setFailure(future.cause());
            }
        });

        if (!promise.awaitUninterruptibly(timeoutMillis)) {
            String message = future.isSuccess() ? SERVER_RESPONSE_TIMEOUT : CHANNEL_WRITE_TIMEOUT;
            throw new StompizeException(message);
        }
        if (!promise.isSuccess()) throw Throwables.propagate(promise.cause());
    }

    private static final String CHANNEL_WRITE_TIMEOUT   = "Channel write timeout.";
    private static final String SERVER_RESPONSE_TIMEOUT = "Server response timeout.";

    private static final ByteBuf NO_CONTENT      = buf("\n\n\u0000");
    private static final ByteBuf FRAME_DELIMITER = buf("\n\n");

    protected final Channel          channel;
    protected final long             timeoutMillis;
    protected final String           command;
    protected final Iterable<Header> headers;
    protected final Content<?>       content;
}
