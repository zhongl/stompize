package com.github.zhongl.stompize;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
abstract class ForeachFrameOf {
    protected ForeachFrameOf(Class<? extends Specification> spec) { methods = spec.getMethods(); }

    public final void apply() {
        for (Method method : methods) {
            if (exclude(method)) continue;

            boolean hasContent = false;

            command(method.getName(), method);

            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            int length = parameterAnnotations.length;
            Class<?>[] parameterTypes = method.getParameterTypes();

            for (int i = 0; i < length; i++) {
                hasContent = foreach(parameterAnnotations[i], i + 1, length, parameterTypes[i], method);
            }

            if (!hasContent) content(-1);
        }
    }

    private boolean foreach(Annotation[] parameterAnnotation, int i, int last, Class<?> type, Method method) {
        for (Annotation annotation : parameterAnnotation) {
            if (annotation instanceof Required) {
                header(((Required) annotation).value(), i, true);
                return false;
            }
            if (annotation instanceof Optional) {
                header(((Optional) annotation).value(), i, false);
                return false;
            }
        }

        if (i != last || type.isPrimitive())
            throw new IllegalArgumentException("Illegal content parameter in " + method + ", it should be last and not be primitive.");

        content(i);
        return true;
    }

    protected abstract void header(String name, int index, boolean required);

    protected abstract boolean exclude(Method method);

    protected abstract void command(String name, Method method);

    protected abstract void content(int index);

    private final Method[] methods;
}