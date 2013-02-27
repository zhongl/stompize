package com.github.zhongl.stompize;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class StompizeException extends RuntimeException {

    public StompizeException(String message) { super(message); }

    public StompizeException(String message, String content) { super(message + '\n' + content); }

    @Override
    public synchronized Throwable fillInStackTrace() { return null; }

    @Override
    public synchronized Throwable initCause(Throwable cause) { return this; }

    private static final long serialVersionUID = -8561559321684011974L;
}
