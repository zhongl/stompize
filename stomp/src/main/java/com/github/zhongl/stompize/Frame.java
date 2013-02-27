package com.github.zhongl.stompize;

import io.netty.buffer.ByteBuf;
import java.util.Map;

import static com.github.zhongl.stompize.Specification.UTF8;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public final class Frame {

    public Frame(ByteBuf command, Map<ByteBuf, ByteBuf> headers, Content content) {
        this.command = command;
        this.headers = headers;
        this.content = content;
    }

    public ByteBuf command() { return command; }

    public Content content() { return content; }

    public String header(ByteBuf name) {
        ByteBuf value = headers.get(name);
        return value == null ? null : value.toString(UTF8);
    }

    private final ByteBuf               command;
    private final Map<ByteBuf, ByteBuf> headers;
    private final Content               content;
}
