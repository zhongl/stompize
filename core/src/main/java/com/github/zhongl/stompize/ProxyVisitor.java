package com.github.zhongl.stompize;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
class ProxyVisitor extends ClassVisitor {

    public static <T extends Specification> String proxyClassName(Class<T> aSpecClass) {
        return aSpecClass.getName() + SUFFIX;
    }

    public ProxyVisitor(Class<? extends Specification> aSpecClass) {
        super(ASM4, new ClassWriter(ClassWriter.COMPUTE_MAXS));
        this.aSpecClass = aSpecClass;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        parent = name;
        self = name + SUFFIX;
        super.visit(version, ACC_PUBLIC + ACC_SUPER, self, null, name, null);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return null;
    }

    @Override
    public void visitEnd() {
        new ForeachFrameOf(aSpecClass) {
            @Override
            protected boolean exclude(Method method) {
                return !Modifier.isAbstract(method.getModifiers());
            }

            @Override
            protected void command(String name, Method method) {
                outputIndex = method.getParameterTypes().length + 1;
                mv = ProxyVisitor.super.visitMethod(ACC_PUBLIC, name, Type.getMethodDescriptor(method), null, null);
                mv.visitCode();

                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, self, "output", "()Lcom/github/zhongl/stompize/FrameVisitor;");
                mv.visitVarInsn(ASTORE, outputIndex);

                mv.visitVarInsn(ALOAD, outputIndex);
                mv.visitLdcInsn(name.toUpperCase());
                mv.visitMethodInsn(INVOKEINTERFACE, "com/github/zhongl/stompize/FrameVisitor", "command", "(Ljava/lang/String;)V");
            }

            @Override
            protected void header(String name, int index, boolean required) {
                mv.visitVarInsn(ALOAD, outputIndex);
                mv.visitLdcInsn(name);
                mv.visitVarInsn(ALOAD, index);
                mv.visitInsn(required ? ICONST_1 : ICONST_0);
                mv.visitMethodInsn(INVOKEINTERFACE, "com/github/zhongl/stompize/FrameVisitor", "header", "(Ljava/lang/String;Ljava/lang/Object;Z)V");
            }

            @Override
            protected void content(int index) {
                mv.visitVarInsn(ALOAD, outputIndex);
                if (index == -1) {
                    mv.visitInsn(ACONST_NULL);
                } else {
                    mv.visitVarInsn(ALOAD, index);
                }
                mv.visitMethodInsn(INVOKEINTERFACE, "com/github/zhongl/stompize/FrameVisitor", "content", "(Ljava/lang/Object;)V");

                mv.visitInsn(RETURN);
                mv.visitMaxs(AUTO_STACK, AUTO_LOCALS);
                mv.visitEnd();
            }

            MethodVisitor mv;
            int outputIndex;
        }.apply();

        super.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, final String name, final String desc, String signature, final String[] exceptions) {
        if ("<init>".equals(name)) {
            MethodVisitor mv = super.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);
            visitInit(name, desc, Type.getArgumentTypes(desc), mv);
        }
        return null;
    }

    private void visitInit(String name, String desc, Type[] argumentTypes, MethodVisitor mv) {
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);

        for (int i = 0, j = 1; i < argumentTypes.length; i++, j++) {
            switch (argumentTypes[i].getSort()) {
                case Type.BOOLEAN:
                case Type.BYTE:
                case Type.CHAR:
                case Type.SHORT:
                case Type.INT:
                    mv.visitVarInsn(ILOAD, j);
                    break;
                case Type.FLOAT:
                    mv.visitVarInsn(FLOAD, j);
                    break;
                case Type.LONG:
                    mv.visitVarInsn(LLOAD, j);
                    j++;
                    break;
                case Type.DOUBLE:
                    mv.visitVarInsn(DLOAD, j);
                    j++;
                    break;
                default:
                    mv.visitVarInsn(ALOAD, j);
            }
        }

        mv.visitMethodInsn(INVOKESPECIAL, parent, name, desc);
        mv.visitInsn(RETURN);

        mv.visitMaxs(AUTO_STACK, AUTO_LOCALS);
        mv.visitEnd();
    }

    public byte[] toByteArray() {
        return ((ClassWriter) cv).toByteArray();
    }

    private static final int    AUTO_STACK  = 0;
    private static final int    AUTO_LOCALS = 0;
    private static final String SUFFIX      = "Stompized";

    private final Class<? extends Specification> aSpecClass;

    private String parent;
    private String self;
}
