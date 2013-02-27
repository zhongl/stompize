package com.github.zhongl.stompize;

import java.lang.reflect.Method;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public final class Stompize {

    @SuppressWarnings("unchecked")
    public static <T extends Specification> T newInstance(Class<T> aSpecClass, Object... arguments) throws Exception {
        String className = ProxyClassWriter.proxyClassName(aSpecClass);
        ClassLoader loader = aSpecClass.getClassLoader();
        Class<?> aClass;
        try {
            aClass = loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            aClass = defineClass(className, new ProxyClassWriter(aSpecClass).toByteArray(), loader);
        }
        // TODO check aSpecClass has only one constructors
        return (T) aClass.getConstructors()[0].newInstance((Object[]) arguments);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Specification> Callback callback(T aSpec) throws Exception {
        Class<T> aSpecClass = (Class<T>) aSpec.getClass().getSuperclass();
        String className = CallbackClassWriter.callbackClassName(aSpecClass);
        ClassLoader loader = aSpecClass.getClassLoader();
        Class<?> aClass;
        try {
            aClass = loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            aClass = defineClass(className, new CallbackClassWriter(aSpecClass).toByteArray(), loader);
        }
        return (Callback) aClass.getConstructor(aSpecClass).newInstance(aSpec);
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
