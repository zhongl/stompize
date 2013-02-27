package com.github.zhongl.stompize;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
class GenerateCommandOut<T> extends GenerateMethods {

    public GenerateCommandOut(ClassVisitor cv, Class<T> specification, Class<? extends T> aClass, String self) {
        super(cv);
        this.specification = specification;
        this.aClass = aClass;
        this.self = self;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (isOverridedOrNotCommand(name, desc)) return null;
        MethodVisitor mv = super.visitMethod(access & ~ACC_ABSTRACT, name, desc, signature, exceptions);
        Type[] argumentTypes = Type.getArgumentTypes(desc);
        int arrayListIndex = argumentTypes.length + 1;

        mv.visitCode();
        mv.visitTypeInsn(NEW, "java/util/ArrayList");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V");
        mv.visitVarInsn(ASTORE, arrayListIndex);

        int contentIndex = -1;
        for (int i = 0; i < argumentTypes.length; i++) {
            Type t = argumentTypes[i];
            if (Stompize.isHeader(t, aClass.getClassLoader())) {
                mv.visitVarInsn(ALOAD, arrayListIndex);
                mv.visitVarInsn(ALOAD, THIS_INDEX);
                mv.visitVarInsn(ALOAD, i + 1);
                mv.visitLdcInsn(Stompize.headerName(t));
                mv.visitMethodInsn(INVOKEVIRTUAL, self, "checkNotNull", "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;");
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z");
                mv.visitInsn(POP);
            } else if (isContent(t)) {
                contentIndex = i + 1;
            } else if (Stompize.isOptionals(t)) {
                mv.visitVarInsn(ALOAD, arrayListIndex);
                mv.visitVarInsn(ALOAD, i + 1);
                mv.visitMethodInsn(INVOKESTATIC, "java/util/Collections", "addAll", "(Ljava/util/Collection;[Ljava/lang/Object;)Z");
                mv.visitInsn(POP);
            } else {
                throw new IllegalArgumentException("Unexpected argument type: " + t.getClassName());
            }
        }

        mv.visitVarInsn(ALOAD, THIS_INDEX);
        mv.visitLdcInsn(name.toUpperCase());
        mv.visitVarInsn(ALOAD, arrayListIndex);

        if (contentIndex == -1) mv.visitInsn(ACONST_NULL);
        else mv.visitVarInsn(ALOAD, contentIndex);

        mv.visitMethodInsn(INVOKEVIRTUAL, self, "out", "(Ljava/lang/String;Ljava/lang/Iterable;Lcom/github/zhongl/stompize/Content;)V");

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);// auto compute stack and locals
        mv.visitEnd();

        return null;
    }

    private boolean isContent(Type t) {
        return Stompize.isContent(t, aClass.getClassLoader());
    }

    private boolean isOverridedOrNotCommand(String name, String desc) {
        Class<?>[] types = Stompize.toClasses(Type.getArgumentTypes(desc), aClass.getClassLoader());
        try {
            aClass.getDeclaredMethod(name, types);
            return true;
        } catch (NoSuchMethodException e) {
            try {
                return specification.getMethod(name, types).getAnnotation(Command.class) == null;
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException("Never be here.", ex);
            }
        }
    }

    private static final int THIS_INDEX = 0;

    private final Class<T>           specification;
    private final Class<? extends T> aClass;
    private final String             self;
}
