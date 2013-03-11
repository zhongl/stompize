package com.github.zhongl.stompize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundByteHandlerAdapter;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class CallbackHandler extends ChannelInboundByteHandlerAdapter {

    public CallbackHandler(int maxFrameLength, Stompizeble stompizeble) {
        parser = new Parser(maxFrameLength, stompizeble);
    }

    @Override
    protected void inboundBufferUpdated(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        while (parser.parse(in)) { }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.channel().close();
    }

    private final Parser parser;
}