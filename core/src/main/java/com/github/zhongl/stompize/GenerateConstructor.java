package com.github.zhongl.stompize;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
class GenerateConstructor {

    public GenerateConstructor(ClassVisitor cv, String parent, String name, String desc, String signature, String[] exceptions) {
        this.cv = cv;
        this.parent = parent;
        this.name = name;
        this.desc = desc;
        this.signature = signature;
        this.exceptions = exceptions;
    }

    public void apply() {
        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);
        Type[] argumentTypes = Type.getArgumentTypes(desc);
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

        mv.visitMaxs(0, 0); // auto compute stack and locals
        mv.visitEnd();

    }

    private final ClassVisitor cv;
    private final String       parent;
    private final String       name;
    private final String       desc;
    private final String       signature;
    private final String[]     exceptions;
}
