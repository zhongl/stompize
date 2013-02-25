package com.github.zhongl.stompize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundByteHandlerAdapter;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public abstract class InboundHandler extends ChannelInboundByteHandlerAdapter {
    protected InboundHandler(int maxFrameLength) {
        parser = new Parser(maxFrameLength);
    }

    @Override
    protected void inboundBufferUpdated(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        for (; ; ) {
            Frame frame = parser.parse(in);
            if (frame == null) return;
            receive(frame);
        }
    }

    protected abstract void receive(Frame frame);

    private final Parser parser;
}
