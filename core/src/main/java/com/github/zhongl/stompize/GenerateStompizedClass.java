package com.github.zhongl.stompize;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static com.google.common.base.Preconditions.checkState;
import static org.objectweb.asm.Opcodes.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
class GenerateStompizedClass<T extends Specification> extends GenerateSubClass<T> {

    public GenerateStompizedClass(Class<T> specification, Class<? extends T> aImplementClass, String suffix) {
        super(aImplementClass, suffix);
        checkState(specification.isInterface(), "%s should be a interface.", specification);
        checkState(Stompizeble.class.isAssignableFrom(aImplementClass), "%s should extend %s.", aImplementClass, Stompizeble.class);
        this.specification = specification;
    }

    @Override
    public MethodVisitor visitMethod(int access, final String name, final String desc, String signature, final String[] exceptions) {
        if (is(ACC_ABSTRACT, access)) throw new IllegalStateException("Abstract method declared.");

        if ("<init>".equals(name)) {
            new GenerateConstructor(cv, parent, name, desc, signature, exceptions).apply();
            return null;
        }

        if (isCommandMethod(name, desc)) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            decorateNullPointValidation(name, desc, mv);
        }

        return null;
    }

    @Override
    public void visitEnd() {
        visit(specification, new GenerateCommandOut<T>(cv, specification, aClass, self));
        visit(Stompizeble.class, new GenerateStompizebleApply<T>(cv, specification, aClass, self));
        super.visitEnd();
    }

    private void decorateNullPointValidation(String name, String desc, MethodVisitor mv) {
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);

        Type[] argumentTypes = Type.getArgumentTypes(desc);
        for (int i = 0; i < argumentTypes.length; i++) {
            Type t = argumentTypes[i];
            if (isHeader(t)) { // add null value check
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, i + 1);
                mv.visitLdcInsn(Stompize.headerName(t));
                mv.visitMethodInsn(INVOKEVIRTUAL, self, "checkNotNull", "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;");
                mv.visitTypeInsn(CHECKCAST, t.getInternalName());
            } else {
                mv.visitVarInsn(ALOAD, i + 1);
            }
        }

        mv.visitMethodInsn(INVOKESPECIAL, parent, name, desc);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0); // auto compute stack and locals
        mv.visitEnd();
    }

    private boolean isHeader(Type t) {
        return Stompize.isHeader(t, aClass.getClassLoader());
    }

    private boolean isCommandMethod(String name, String desc) {
        return Stompize.isCommandMethod(name, types(desc), specification);
    }

    @SuppressWarnings("unchecked")
    private Class<?>[] types(String desc) {
        return Stompize.toClasses(Type.getArgumentTypes(desc), aClass.getClassLoader());
    }

    private static boolean is(int opcode, int access) { return (access & opcode) == opcode; }

    private final Class<T> specification;
}
