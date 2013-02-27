package com.github.zhongl.stompize;

import java.lang.reflect.Method;

import static com.github.zhongl.stompize.Stompize.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
abstract class ForeachFrameOf {
    protected ForeachFrameOf(Class<?> spec) { methods = spec.getMethods(); }

    public final void apply() {
        for (Method method : methods) {
            if (exclude(method)) continue;

            boolean hasContent = false;

            command(method.getName(), method);

            Class<?>[] pts = method.getParameterTypes();
            for (int i = 0; i < pts.length; i++) {
                Class<?> pt = pts[i];

                if (isHeader(pt)) {
                    required(headerName(pt.getSimpleName()), i + 1);
                } else if (isContent(pt)) {
                    content(i + 1);
                    hasContent = true;
                } else if (isOptionals(pt)) {
                    optionals(i + 1);
                } else {
                    throw new IllegalArgumentException("Unexpect argument type:" + pt);
                }
            }

            if (!hasContent) content(-1);

            end();
        }
    }

    protected void end() {}

    protected abstract boolean exclude(Method method);

    protected abstract void command(String name, Method method);

    protected abstract void required(String headerName, int index);

    protected abstract void content(int index);

    protected abstract void optionals(int index);

    private final Method[] methods;
}