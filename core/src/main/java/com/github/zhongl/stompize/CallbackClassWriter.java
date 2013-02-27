package com.github.zhongl.stompize;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
class CallbackClassWriter extends ClassVisitor {

    public static String callbackClassName(Class<?> aSpecClass) {return aSpecClass.getName() + SUFFIX;}

    public CallbackClassWriter(Class<? extends Specification> aSpecClass) {
        super(ASM4, new ClassWriter(ClassWriter.COMPUTE_MAXS));
        this.aSpecClass = aSpecClass;
        aSpecClassName = Type.getInternalName(aSpecClass);
        aSpecClassDescriptor = Type.getDescriptor(aSpecClass);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        self = aSpecClassName + SUFFIX;

        super.visit(version, ACC_PUBLIC, self, null, Type.getInternalName(Object.class), new String[]{name});

        visitField(ACC_PRIVATE + ACC_FINAL, SPEC_FIELD, aSpecClassDescriptor, null, null).visitEnd();

        MethodVisitor mv = super.visitMethod(ACC_PUBLIC, "<init>", '(' + aSpecClassDescriptor + ")V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, self, SPEC_FIELD, aSpecClassDescriptor);
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();

    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);

        final String frame = Type.getInternalName(Frame.class);

        new ForeachFrameOf(aSpecClass) {

            @Override
            protected boolean exclude(Method method) {
                return Modifier.isAbstract(method.getModifiers()) || !method.getDeclaringClass().equals(aSpecClass);
            }

            @Override
            protected void command(String name, Method method) {
                this.method = method;
                mv.visitCode();
                mv.visitVarInsn(ALOAD, 1);
                mv.visitLdcInsn(name.toUpperCase());
                mv.visitMethodInsn(INVOKEINTERFACE, frame, "isCommand", "(Ljava/lang/String;)Z");

                label = new Label();
                mv.visitJumpInsn(IFEQ, label);

                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, self, SPEC_FIELD, aSpecClassDescriptor);
            }

            @Override
            protected void header(String name, int index, boolean required) {
                mv.visitVarInsn(ALOAD, 1);
                mv.visitLdcInsn(name);
                mv.visitMethodInsn(INVOKEINTERFACE, "com/github/zhongl/stompize/Frame", "header", "(Ljava/lang/String;)Ljava/lang/Object;");
                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(method.getParameterTypes()[index - 1]));
            }

            @Override
            protected void content(int index) {
                if (index != -1) {
                    mv.visitVarInsn(ALOAD, 1);
                    mv.visitMethodInsn(INVOKEINTERFACE, frame, "content", "()Ljava/lang/Object;");
                }

                mv.visitMethodInsn(INVOKEVIRTUAL, aSpecClassName, method.getName(), Type.getMethodDescriptor(method));

                mv.visitInsn(RETURN);
                mv.visitLabel(label);
                mv.visitFrame(F_SAME, 0, null, 0, null);

            }

            Method method;
            Label label;
        }.apply();

        mv.visitInsn(RETURN);

        mv.visitMaxs(0, 0); // auto compute stack and locals
        mv.visitEnd();

        return null;
    }

    public byte[] toByteArray() throws IOException {
        String name = Type.getInternalName(Callback.class) + ".class";
        ClassReader cr = new ClassReader(aSpecClass.getClassLoader().getResourceAsStream(name));
        cr.accept(this, ClassReader.SKIP_DEBUG);
        byte[] bytes = ((ClassWriter) cv).toByteArray();
        return bytes;
    }

    private static final String SUFFIX     = "Callback";
    private static final String SPEC_FIELD = "spec";

    private final Class<? extends Specification> aSpecClass;

    private       String self;
    private final String aSpecClassName;
    private final String aSpecClassDescriptor;
}
