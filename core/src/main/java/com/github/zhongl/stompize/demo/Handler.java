package com.github.zhongl.stompize.demo;

import com.github.zhongl.stompize.Frame;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundByteHandlerAdapter;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class Handler extends ChannelInboundByteHandlerAdapter {
    private final Demo delegate;

    public Handler(Demo delegate) {this.delegate = delegate;}

    @Override
    protected void inboundBufferUpdated(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        for (; ; ) {
            Frame frame = Frame.decode(in);

            if (frame == null) return;

            if ("SEND".equals(frame.command())) {
                delegate.send(
                        frame.header("destination"),
                        frame.header("transaction"),
                        frame.header("receipt"),
                        frame.content()
                );
            } else {
                delegate.receipt(frame.header("receipt-id"));
            }

        }
    }


}
