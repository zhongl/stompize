package com.github.zhongl.stompize;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public final class Bytes {

    public static ByteBuf buf(String s) {return Unpooled.wrappedBuffer(Specification.bytes(s));}

    private Bytes() {}
}
