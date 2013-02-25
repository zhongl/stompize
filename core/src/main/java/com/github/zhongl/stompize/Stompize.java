package com.github.zhongl.stompize;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundByteHandler;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import static com.github.zhongl.stompize.Bytes.UTF8;
import static com.github.zhongl.stompize.Bytes.buf;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public final class Stompize {

    @SuppressWarnings("unchecked")
    public static <T extends Specification> T newInstance(Class<T> type, Channel channel, Object... arguments)
            throws Exception {
        return (T) sub(type, OutboundClassWriter.class).newInstance((Object[]) concat(channel, arguments));
    }

    public static <T extends Specification> ChannelInboundByteHandler inboundHandler(T object, int maxFrameLength)
            throws Exception {
        return (ChannelInboundByteHandler) sub(object.getClass().getSuperclass(), InboundClassWriter.class).newInstance(object, maxFrameLength);
    }

    private static Constructor<?> sub(Class<?> type, Class<? extends StompizedClassWriter> writerClass) throws Exception {
        String suffix = writerClass.getSimpleName().replace("ClassWriter", "");
        String subClassName = type.getName() + suffix;
        ClassLoader loader = type.getClassLoader();
        Class<?> aClass;
        try {
            aClass = loader.loadClass(subClassName);
        } catch (ClassNotFoundException e) {
            byte[] bytecode = writerClass.getConstructor(Class.class, String.class)
                                         .newInstance(type, suffix).toByteArray();
            aClass = defineClass(subClassName, bytecode, loader);
        }
        return aClass.getConstructors()[0];
    }

    private static Object[] concat(Channel channel, Object[] arguments) {
        Object[] objects = new Object[arguments.length + 1];
        objects[0] = channel;
        System.arraycopy(arguments, 0, objects, 1, arguments.length);
        return objects;
    }

    private static Class<?> defineClass(String subClassName, byte[] bytecode, ClassLoader loader)
            throws Exception {
        Class<?>[] parameterTypes = {String.class, byte[].class, int.class, int.class};
        Method method = ClassLoader.class.getDeclaredMethod("defineClass", parameterTypes);
        method.setAccessible(true);
        Object[] args = {subClassName, bytecode, Integer.valueOf(0), Integer.valueOf(bytecode.length)};
        return (Class<?>) method.invoke(loader, args);
    }

    public static void addRequiredHeaderTo(List<ByteBuf> components, ByteBuf name, String value) {
        if (value == null) throw new IllegalArgumentException(missingRequiredHeader(name));
        components.add(name);
        components.add(buf(value));
    }

    public static void addOptionalHeaderTo(List<ByteBuf> components, ByteBuf name, String value) {
        if (value == null) return;
        components.add(name);
        components.add(buf(value));
    }

    public static void addContentTo(List<ByteBuf> components, Content content) {
        if (content == null) {
            Content.NONE.appendTo(components);
        } else {
            content.appendTo(components);
        }
    }

    private static String missingRequiredHeader(ByteBuf name) {
        return "Missing required header: " + name.toString(UTF8).trim().replace(":", "");
    }

    private Stompize() {}
}
