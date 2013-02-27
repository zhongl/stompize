package com.github.zhongl.stompize;

import static com.github.zhongl.stompize.Stompize.headerName;
import static com.google.common.base.Preconditions.checkNotNull;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public abstract class Header {

    protected Header(String value) { this.value = checkNotNull(value); }

    public final String value() { return value; }

    public final String name() { return headerName(getClass()); }

    @Override
    public final String toString() { return '\n' + name() + ':' + value; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Header)) return false;

        Header header = (Header) o;

        if (!toString().equals(header.toString())) return false;

        return true;
    }

    @Override
    public int hashCode() { return value.hashCode(); }

    private final String value;

}
