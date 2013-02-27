package com.github.zhongl.stompize;

import static com.google.common.base.Preconditions.checkNotNull;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public abstract class Content<T> {

    protected Content(T value, String type) {
        this.value = checkNotNull(value);
        this.type = checkNotNull(type);
    }

    public String type() { return type; }

    public int length() {return value().length();}

    @Override
    public String toString() { return "\n\n" + value() + '\u0000'; }

    protected abstract String value();

    protected final T      value;
    protected final String type;
}
