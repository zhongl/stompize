package com.github.zhongl.stompize;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import org.objectweb.asm.Type;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public final class Stompize {

    @SuppressWarnings("unchecked")
    public static <T extends Specification> T create(Class<T> specification, Class<? extends T> aImplementClass, Object... arguments) throws Exception {
        String suffix = "Stompized";
        String className = aImplementClass.getName() + suffix;
        ClassLoader loader = aImplementClass.getClassLoader();
        Class<?> aClass;
        try {
            aClass = loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            aClass = new GenerateStompizedClass(specification, aImplementClass, suffix).apply();
        }
        // TODO check aImplementClass has only one constructors
        return (T) aClass.getConstructors()[0].newInstance((Object[]) arguments);
    }

    static Class<?> toClass(Type t, ClassLoader loader) {
        try {
            if (t.getSort() != Type.ARRAY) return loader.loadClass(t.getClassName());

            Type elementType = t.getElementType();
            Class<?> c = loader.loadClass(elementType.getClassName());
            return Array.newInstance(c, 0).getClass();
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    static boolean isCommandMethod(String name, Class<?>[] types, Class<?> specification) {
        try {
            Method method = specification.getMethod(name, types);
            return method.getAnnotation(Command.class) != null;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    static Class<?>[] toClasses(Type[] types, ClassLoader loader) {
        Class<?>[] classes = (Class<?>[]) Array.newInstance(Class.class, types.length);
        for (int i = 0; i < types.length; i++) {
            classes[i] = toClass(types[i], loader);
        }
        return classes;
    }

    static boolean isHeader(Type t, ClassLoader loader) { return isHeader(toClass(t, loader)); }

    static boolean isHeader(Class<?> cls) {return Header.class.isAssignableFrom(cls);}

    static boolean isContent(Type t, ClassLoader loader) { return isContent(toClass(t, loader)); }

    static boolean isContent(Class<?> cls) {return Content.class.isAssignableFrom(cls);}

    static boolean isOptionals(Type t) { return t.equals(Type.getType(Header[].class)); }

    static boolean isOptionals(Class<?> cls) { return Header[].class.equals(cls); }

    static String headerName(Type t) {
        String internalName = t.getInternalName();
        CharMatcher cm = CharMatcher.anyOf("/$");
        String last = Iterables.getLast(Splitter.on(cm).split(internalName));
        return headerName(last);
    }

    static String headerName(Class<?> c) {
        String internalName = c.getName();
        CharMatcher cm = CharMatcher.anyOf(".$");
        String last = Iterables.getLast(Splitter.on(cm).split(internalName));
        return headerName(last);
    }

    static String headerName(String s) {
        int n = CharMatcher.JAVA_UPPER_CASE.countIn(s);
        if (n == 1) return s.toLowerCase();
        StringBuilder sb = new StringBuilder(s.length() + (n - 1) * 2);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) sb.append('-');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    static String className(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(s.charAt(0)));

        for (int i = 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '-') {
                sb.append(Character.toUpperCase(s.charAt(++i)));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private Stompize() {}
}
