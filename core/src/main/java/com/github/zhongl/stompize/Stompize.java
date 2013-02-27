package com.github.zhongl.stompize;

import java.io.IOException;
import java.lang.reflect.Method;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public final class Stompize {

    @SuppressWarnings("unchecked")
    public static <T extends Specification> T newInstance(Class<T> aSpecClass, Object... arguments) throws Exception {
        String className = ProxyVisitor.proxyClassName(aSpecClass);
        ClassLoader loader = aSpecClass.getClassLoader();
        Class<?> aClass;
        try {
            aClass = loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            aClass = defineClass(className, generateSub(aSpecClass), loader);
        }
        // TODO check aSpecClass has only one constructors
        return (T) aClass.getConstructors()[0].newInstance((Object[]) arguments);
    }

    private static byte[] generateSub(Class<?> aSpecClass) throws IOException {
        String name = Type.getInternalName(aSpecClass) + ".class";
        ProxyVisitor pv = new ProxyVisitor(aSpecClass);
        ClassReader cr = new ClassReader(aSpecClass.getClassLoader().getResourceAsStream(name));
        cr.accept(pv, ClassReader.SKIP_DEBUG);
        return pv.toByteArray();
    }

    private static Class<?> defineClass(String subClassName, byte[] bytecode, ClassLoader loader) throws Exception {
        Object[] args = {subClassName, bytecode, Integer.valueOf(0), Integer.valueOf(bytecode.length)};
        Class<?>[] parameterTypes = {String.class, byte[].class, int.class, int.class};
        Method method = ClassLoader.class.getDeclaredMethod("defineClass", parameterTypes);
        method.setAccessible(true);
        return (Class<?>) method.invoke(loader, args);
    }

    private Stompize() {}

}
