package com.github.zhongl.stompize;

import java.nio.charset.Charset;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public abstract class Specification {
    public static final byte    LF    = 0x0A;
    public static final byte    CR    = 0x0D;
    public static final byte    NULL  = 0x00;
    public static final byte    COLON = 0x3A;
    public static final Charset UTF8  = Charset.forName("UTF-8");

    public static byte[] bytes(String s) {return s.getBytes(UTF8);}

    /** @return {@link FrameVisitor} used to output method invocation as a frame. */
    protected abstract FrameVisitor output();
}
