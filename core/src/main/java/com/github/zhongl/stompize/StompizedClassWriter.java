package com.github.zhongl.stompize;

import com.github.zhongl.stompize.demo.DemoClientImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * Generate concreted bytecode of {@link Specification}.
 * <p/>
 * {@link DemoClientImpl} would help you to understand the source below.
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class StompizedClassWriter {
    private static final String DESC_BYTE_BUF = Type.getDescriptor(ByteBuf.class);
    private static final String DESC_CHANNEL  = Type.getDescriptor(Channel.class);
    private static final String CHANNEL       = "channel";

    private final Class<? extends Specification> spec;
    private final String                         className;
    private final String                         subclassName;
    private final ClassWriter                    cw;

    public StompizedClassWriter(Class<? extends Specification> spec, String suffix) {
        this.spec = spec;
        className = Type.getInternalName(spec);
        subclassName = className + suffix;
        cw = new ClassWriter(0);
    }

    public byte[] toByteArray() {
        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, subclassName, null, className, null);

        staticFields();

        field(ACC_PRIVATE + ACC_FINAL, CHANNEL, DESC_CHANNEL);

        clinit();

        init();

        methods();

        cw.visitEnd();

        return cw.toByteArray();
    }

    private void staticFields() {
        new ForeachCommandOf(spec) {
            Set<String> applied = new HashSet<String>();

            @Override
            protected void applyCommand(String name, Method method) {
                String u = name.toUpperCase();
                if (applied.contains(u)) return;
                field(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, u, DESC_BYTE_BUF);
                applied.add(u);
            }

            @Override
            protected void applyHeader(String name, int index) {
                if (applied.contains(name)) return;
                field(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, name, DESC_BYTE_BUF);
                applied.add(name);
            }
        }.apply();
    }

    private void clinit() {
        final MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();

        new ForeachCommandOf(spec) {
            Set<String> applied = new HashSet<String>();

            @Override
            protected void applyCommand(String name, Method method) {
                String u = name.toUpperCase();
                if (applied.contains(u)) return;
                initField(u, u);
                applied.add(u);
            }

            @Override
            protected void applyHeader(String name, int index) {
                if (applied.contains(name)) return;
                initField(name, name);
                applied.add(name);
            }

            private void initField(String name, String value) {
                mv.visitLdcInsn(value);
                mv.visitMethodInsn(INVOKESTATIC, "com/github/zhongl/stompize/Bytes", "buf", "(Ljava/lang/String;)Lio/netty/buffer/ByteBuf;");
                mv.visitFieldInsn(PUTSTATIC, subclassName, name, DESC_BYTE_BUF);
            }

        }.apply();

        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 0);
        mv.visitEnd();
    }

    private void init() {
        MethodVisitor mv;
        Constructor<?>[] constructors = spec.getDeclaredConstructors();
        if (constructors.length > 1)
            throw new IllegalArgumentException("Only one constructor should be declared in " + spec);

        Class<?>[] parameterTypes = constructors[0].getParameterTypes();

        StringBuilder descriptor = new StringBuilder("(");
        descriptor.append(DESC_CHANNEL);
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
        mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", Type.getConstructorDescriptor(constructors[0]));

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, subclassName, CHANNEL, DESC_CHANNEL);

        mv.visitInsn(RETURN);
        mv.visitMaxs(parameterTypes.length + 1, parameterTypes.length + 2);
        mv.visitEnd();
    }

    private void methods() {
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
                mv.visitFieldInsn(GETSTATIC, subclassName, name.toUpperCase(), DESC_BYTE_BUF);
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
                mv.visitInsn(POP);
            }

            @Override
            protected void applyRequiredHeader(String name, int index) {
                mv.visitVarInsn(ALOAD, componentsIndex);
                mv.visitFieldInsn(GETSTATIC, subclassName, name, DESC_BYTE_BUF);
                mv.visitVarInsn(ALOAD, index);
                mv.visitMethodInsn(INVOKESTATIC, "com/github/zhongl/stompize/Stompize", "addRequiredHeaderTo", "(Ljava/util/List;Lio/netty/buffer/ByteBuf;Ljava/lang/String;)V");
            }

            @Override
            protected void applyOptionalHeader(String name, int index) {
                mv.visitVarInsn(ALOAD, componentsIndex);
                mv.visitFieldInsn(GETSTATIC, subclassName, name, DESC_BYTE_BUF);
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
                mv.visitFieldInsn(GETFIELD, subclassName, CHANNEL, DESC_CHANNEL);
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
                mv.visitFieldInsn(GETFIELD, subclassName, CHANNEL, DESC_CHANNEL);
                mv.visitMethodInsn(INVOKEINTERFACE, "io/netty/channel/Channel", "flush", "()Lio/netty/channel/ChannelFuture;");
                mv.visitInsn(POP);

                mv.visitInsn(RETURN);

                mv.visitMaxs(7, componentsIndex + 1);
                mv.visitEnd();

            }
        }.apply();
    }

    private void field(int access, String name, String type) {
        cw.visitField(access, name, type, null, null).visitEnd();
    }
}
