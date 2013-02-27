package com.github.zhongl.stompize;

import org.junit.Test;
import org.objectweb.asm.MethodVisitor;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public class GenerateConstructorTest {

    @Test
    public void shouldGeneratSubA() throws Exception {
        final String suffix = "gen";

        Class<?> aClass = new GenerateSubClass<A>(A.class, suffix) {

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if ("<init>".equals(name)) {
                    new GenerateConstructor(cv, parent, name, desc, signature, exceptions).apply();
                    return null;
                }
                return super.visitMethod(access, name, desc, signature, exceptions);
            }

        }.apply();
        aClass.getConstructors()[0].newInstance(false, Byte.MAX_VALUE, 'a', Short.MAX_VALUE, 1, Float.MAX_VALUE, Long.MAX_VALUE, Double.MAX_VALUE, "123");
    }


    /** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
    abstract static class A {
        A(boolean z, byte b, char c, short s, int i, float f, long l, double d, Object o) { }
    }
}
