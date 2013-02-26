package com.github.zhongl.stompize;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public interface FrameVisitor {
    /** First call. */
    void command(byte[] command);

    /** Call when meet a required header. */
    void required(byte[] name, Object value);

    /** Call when meet a optional header. */
    void optional(byte[] name, Object value);

    /** Last call, value may be null if no content */
    void content(Object value);
}
