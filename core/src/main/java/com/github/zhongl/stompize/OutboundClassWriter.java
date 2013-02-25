package com.github.zhongl.stompize;

import com.github.zhongl.stompize.demo.DemoClientImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * {@link DemoClientImpl} would help you to understand the source below.
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class OutboundClassWriter extends StompizedClassWriter {

    public OutboundClassWriter(Class<? extends Specification> spec, String suffix) {
        super(spec, suffix);
    }

    @Override
    protected String superClassName() { return className; }

    @Override
    protected void fields() {field(ACC_PRIVATE + ACC_FINAL, "channel", Type.getDescriptor(Channel.class));}

    @Override
    protected void init() {
        MethodVisitor mv;
        Constructor<?>[] constructors = spec.getDeclaredConstructors();
        if (constructors.length > 1)
            throw new IllegalArgumentException("Only one constructor should be declared in " + spec);

        Class<?>[] parameterTypes = constructors[0].getParameterTypes();

        StringBuilder descriptor = new StringBuilder("(");
        descriptor.append(Type.getDescriptor(Channel.class));
        for (Class<?> parameterType : parameterTypes) descriptor.append(Type.getDescriptor(parameterType));
        descriptor.append(")V");

        mv = cw.visitMethod(ACC_PUBLIC, "<init>", descriptor.toString(), null, null);
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].isPrimitive())
                throw new IllegalArgumentException("Primitive constructor parameter is unsupported in " + spec);

            mv.visitVarInsn(ALOAD, i + 2);
        }
        mv.visitMethodInsn(INVOKESPECIAL, superClassName(), "<init>", Type.getConstructorDescriptor(constructors[0]));

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, subClassName, "channel", Type.getDescriptor(Channel.class));

        mv.visitInsn(RETURN);
        mv.visitMaxs(parameterTypes.length + 1, parameterTypes.length + 2);
        mv.visitEnd();
    }

    @Override
    protected void methods() {
        new ForeachCommandOf(spec) {
            MethodVisitor mv;
            int componentsIndex;

            @Override
            protected void applyCommand(String name, Method method) {
                componentsIndex = method.getParameterTypes().length + 1;
                mv = cw.visitMethod(ACC_PUBLIC, name, Type.getMethodDescriptor(method), null, null);
                mv.visitCode();

                mv.visitTypeInsn(NEW, "java/util/ArrayList");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
                mv.visitVarInsn(ASTORE, componentsIndex);

                mv.visitVarInsn(ALOAD, componentsIndex);
                mv.visitFieldInsn(GETSTATIC, subClassName, name.toUpperCase(), Type.getDescriptor(ByteBuf.class));
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
                mv.visitInsn(POP);
            }

            @Override
            protected void applyRequiredHeader(String name, int index) {
                mv.visitVarInsn(ALOAD, componentsIndex);
                mv.visitFieldInsn(GETSTATIC, subClassName, name, Type.getDescriptor(ByteBuf.class));
                mv.visitVarInsn(ALOAD, index);
                mv.visitMethodInsn(INVOKESTATIC, "com/github/zhongl/stompize/Stompize", "addRequiredHeaderTo", "(Ljava/util/List;Lio/netty/buffer/ByteBuf;Ljava/lang/String;)V");
            }

            @Override
            protected void applyOptionalHeader(String name, int index) {
                mv.visitVarInsn(ALOAD, componentsIndex);
                mv.visitFieldInsn(GETSTATIC, subClassName, name, Type.getDescriptor(ByteBuf.class));
                mv.visitVarInsn(ALOAD, index);
                mv.visitMethodInsn(INVOKESTATIC, "com/github/zhongl/stompize/Stompize", "addOptionalHeaderTo", "(Ljava/util/List;Lio/netty/buffer/ByteBuf;Ljava/lang/String;)V");
            }

            @Override
            protected void applyContent(int index) {
                mv.visitVarInsn(ALOAD, componentsIndex);
                if (index > 0) mv.visitVarInsn(ALOAD, index);
                else mv.visitInsn(ACONST_NULL);
                mv.visitMethodInsn(INVOKESTATIC, "com/github/zhongl/stompize/Stompize", "addContentTo", "(Ljava/util/List;Lcom/github/zhongl/stompize/Content;)V");
            }

            @Override
            protected void applyEnd() {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, subClassName, "channel", Type.getDescriptor(Channel.class));
                mv.visitTypeInsn(NEW, "io/netty/buffer/DefaultCompositeByteBuf");
                mv.visitInsn(DUP);
                mv.visitFieldInsn(GETSTATIC, "io/netty/buffer/UnpooledByteBufAllocator", "HEAP_BY_DEFAULT", "Lio/netty/buffer/UnpooledByteBufAllocator;");
                mv.visitInsn(ICONST_0);
                mv.visitIntInsn(BIPUSH, 32); // maxNumComponents
                mv.visitVarInsn(ALOAD, componentsIndex);
                mv.visitMethodInsn(INVOKESPECIAL, "io/netty/buffer/DefaultCompositeByteBuf", "<init>", "(Lio/netty/buffer/ByteBufAllocator;ZILjava/lang/Iterable;)V");
                mv.visitMethodInsn(INVOKEINTERFACE, "io/netty/channel/Channel", "write", "(Ljava/lang/Object;)Lio/netty/channel/ChannelFuture;");
                mv.visitInsn(POP);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, subClassName, "channel", Type.getDescriptor(Channel.class));
                mv.visitMethodInsn(INVOKEINTERFACE, "io/netty/channel/Channel", "flush", "()Lio/netty/channel/ChannelFuture;");
                mv.visitInsn(POP);

                mv.visitInsn(RETURN);

                mv.visitMaxs(7, componentsIndex + 1);
                mv.visitEnd();

            }
        }.apply();
    }

    @Override
    protected String valueOf(String name) {return '\n' + name + ':';}
}
