package com.github.zhongl.stompize;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public abstract class ForeachFrameOf {
    protected ForeachFrameOf(Class<? extends Specification> spec) {
        methods = spec.getMethods();
    }

    public final void apply() {
        for (Method method : methods) {
            if (!Modifier.isAbstract(method.getModifiers())) continue;

            boolean hasContent = false;

            applyCommand(method.getName(), method);

            Annotation[][] parameterAnnotations = method.getParameterAnnotations();

            for (int i = 0; i < parameterAnnotations.length; i++) {
                Annotation[] parameterAnnotation = parameterAnnotations[i];

                for (Annotation annotation : parameterAnnotation) {
                    if (annotation instanceof Required) {
                        applyRequiredHeader(((Required) annotation).value(), i + 1);
                        continue;
                    }
                    if (annotation instanceof Optional) {
                        applyOptionalHeader(((Optional) annotation).value(), i + 1);
                        continue;
                    }
                    throw new IllegalArgumentException("Parameter without Required nor Optional annotation should be Content in " + method);
                }

                if (Content.class.isAssignableFrom(method.getParameterTypes()[i])) {
                    hasContent = true;
                    applyContent(i + 1);
                }
            }

            if (!hasContent) applyContent(-1);

            applyEnd();
        }

    }

    protected void applyCommand(String name, Method method) {}

    protected void applyRequiredHeader(String name, int index) {
        applyHeader(name, index);
    }

    protected void applyOptionalHeader(String name, int index) {
        applyHeader(name, index);
    }

    protected void applyHeader(String name, int index) {}

    protected void applyContent(int index) {}

    protected void applyEnd() {}

    private final Method[] methods;
}
