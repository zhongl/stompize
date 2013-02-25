package com.github.zhongl.stompize;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/**
 * Generate concreted bytecode of {@link Specification}.
 *
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
abstract class StompizedClassWriter {


    protected StompizedClassWriter(Class<? extends Specification> spec, String suffix) {
        this.spec = spec;
        className = Type.getInternalName(spec);
        subClassName = className + suffix;
        cw = new ClassWriter(0);
    }

    public final byte[] toByteArray() {
        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, subClassName, null, superClassName(), null);

        staticFields();

        fields();

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
                field(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, u, Type.getDescriptor(ByteBuf.class));
                applied.add(u);
            }

            @Override
            protected void applyHeader(String name, int index) {
                if (applied.contains(name)) return;
                field(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, name, Type.getDescriptor(ByteBuf.class));
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
                initField(name, valueOf(name));
                applied.add(name);
            }

            private void initField(String name, String value) {
                mv.visitLdcInsn(value);
                mv.visitMethodInsn(INVOKESTATIC, "com/github/zhongl/stompize/Bytes", "buf", "(Ljava/lang/String;)Lio/netty/buffer/ByteBuf;");
                mv.visitFieldInsn(PUTSTATIC, subClassName, name, Type.getDescriptor(ByteBuf.class));
            }

        }.apply();

        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 0);
        mv.visitEnd();
    }

    protected abstract String valueOf(String name);

    protected abstract String superClassName();

    protected abstract void fields();

    protected abstract void init();

    protected abstract void methods();

    protected void field(int access, String name, String type) {
        cw.visitField(access, name, type, null, null).visitEnd();
    }

    protected final Class<? extends Specification> spec;
    protected final String                         className;
    protected final String                         subClassName;
    protected final ClassWriter                    cw;
}
