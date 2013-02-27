package com.github.zhongl.stompize;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;

import static com.github.zhongl.stompize.Bytes.buf;
import static com.github.zhongl.stompize.Specification.*;
import static io.netty.buffer.Unpooled.EMPTY_BUFFER;
import static io.netty.buffer.Unpooled.wrappedBuffer;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class Content {
    private static final ByteBuf CONTENT_TYPE   = buf("\ncontent-type:");
    private static final ByteBuf CONTENT_LENGTH = buf("\ncontent-length:");
    private static final String  TEXT_PLAIN     = "text/plain";

    private static final ByteBuf LF_LF_BUF      = wrappedBuffer(new byte[]{LF, LF});
    private static final ByteBuf NULL_BUF       = wrappedBuffer(new byte[]{NULL});
    private static final ByteBuf LF_LF_NULL_BUF = wrappedBuffer(new byte[]{LF, LF, NULL});

    public static final Content NONE = new Content(EMPTY_BUFFER);

    private final String  type;
    private final ByteBuf value;

    public Content(ByteBuf value) {
        this(null, value);
    }

    public Content(String type, ByteBuf value) {
        this.type = type == null ? TEXT_PLAIN : type;
        this.value = value == null ? EMPTY_BUFFER : value;
    }

    public String type() {return type; }

    public ByteBuf value() {return value.duplicate();}

    public void appendTo(List<ByteBuf> components) {
        int length = value.readableBytes();
        if (length == 0) {
            components.add(LF_LF_NULL_BUF);
        } else {
            components.add(CONTENT_TYPE);
            components.add(Bytes.buf(type));
            components.add(CONTENT_LENGTH);
            components.add(Unpooled.wrappedBuffer(String.valueOf(length).getBytes(UTF8)));
            components.add(LF_LF_BUF);
            components.add(value());
            components.add(NULL_BUF);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Content content = (Content) o;

        if (!type.equals(content.type)) return false;
        if (!value.equals(content.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() { return "Content{type='" + type + '\'' + ", value=" + value + '}'; }
}
