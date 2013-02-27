package com.github.zhongl.stompize;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public interface Frame {
    boolean isCommand(String name);

    Object header(String name);

    Object content();
}
