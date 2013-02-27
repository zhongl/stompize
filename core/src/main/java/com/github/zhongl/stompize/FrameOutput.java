package com.github.zhongl.stompize;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public interface FrameOutput {
    /** First call. */
    void command(String command);

    void header(String name, Object value, boolean required);

    /** Last call, value may be null if no content */
    void content(Object value);
}
