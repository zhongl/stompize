package com.github.zhongl.stompize;

import com.github.zhongl.stompize.demo.DemoHandler;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * {@link DemoHandler} would help you to understand the source below.
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
class InboundClassWriter extends StompizedClassWriter {

    public InboundClassWriter(Class<? extends Specification> spec, String suffix) {
        super(spec, suffix);
        specDescriptor = Type.getDescriptor(spec);
    }

    @Override
    protected String superClassName() { return Type.getInternalName(InboundHandler.class); }

    @Override
    protected void fields() { field(ACC_PRIVATE + ACC_FINAL, SPEC_NAME, specDescriptor); }

    @Override
    protected void init() {
        MethodVisitor mv;

        StringBuilder descriptor = new StringBuilder("(");
        descriptor.append(specDescriptor);
        descriptor.append(Type.getDescriptor(int.class));
        descriptor.append(")V");

        mv = cw.visitMethod(ACC_PUBLIC, "<init>", descriptor.toString(), null, null);
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitMethodInsn(INVOKESPECIAL, superClassName(), "<init>", "(I)V");

        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, subClassName, SPEC_NAME, specDescriptor);

        mv.visitInsn(RETURN);

        autoVisitMaxs(mv);

        mv.visitEnd();

    }

    @Override
    protected void methods() {
        final AtomicInteger stackSize = new AtomicInteger();

        final MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, "receive", "(Lcom/github/zhongl/stompize/Frame;)V", null, null);
        mv.visitCode();

        new ForeachFrameOf(spec) {
            Label label;
            String name;
            String descriptor;

            @Override
            protected void applyCommand(String name, Method method) {
                this.name = name;
                descriptor = Type.getMethodDescriptor(method);

                mv.visitVarInsn(ALOAD, 1);
                mv.visitMethodInsn(INVOKEVIRTUAL, "com/github/zhongl/stompize/Frame", "command", "()Lio/netty/buffer/ByteBuf;");
                mv.visitFieldInsn(GETSTATIC, subClassName, name.toUpperCase(), "Lio/netty/buffer/ByteBuf;");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z");
                label = new Label();
                mv.visitJumpInsn(IFEQ, label);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, subClassName, SPEC_NAME, specDescriptor);
            }

            @Override
            protected void applyHeader(String name, int index) {
                stackSize.incrementAndGet();
                mv.visitVarInsn(ALOAD, 1);
                mv.visitFieldInsn(GETSTATIC, subClassName, name, "Lio/netty/buffer/ByteBuf;");
                mv.visitMethodInsn(INVOKEVIRTUAL, "com/github/zhongl/stompize/Frame", "header", "(Lio/netty/buffer/ByteBuf;)Ljava/lang/String;");
            }

            @Override
            protected void applyContent(int index) {
                if (index == -1) return;
                stackSize.incrementAndGet();
                mv.visitVarInsn(ALOAD, 1);
                mv.visitMethodInsn(INVOKEVIRTUAL, "com/github/zhongl/stompize/Frame", "content", "()Lcom/github/zhongl/stompize/Content;");
            }

            @Override
            protected void applyEnd() {
                mv.visitMethodInsn(INVOKEVIRTUAL, className, name, descriptor);

                mv.visitInsn(RETURN);
                mv.visitLabel(label);

                mv.visitFrame(F_SAME, 0, null, 0, null);
            }
        }.apply();

        mv.visitInsn(RETURN);

        autoVisitMaxs(mv);

        mv.visitEnd();
    }

    @Override
    protected String valueOf(String name) {return name;}

    private static final String SPEC_NAME = "spec";

    private final String specDescriptor;
}
