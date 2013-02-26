package com.github.zhongl.stompize;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
public final class Stompize {

    @SuppressWarnings("unchecked")
    public static <T extends Specification> T newInstance(Class<T> aSpecClass, Object... arguments) throws Exception {
        String className = aSpecClass.getName() + SUFFIX;
        ClassLoader loader = aSpecClass.getClassLoader();
        Class<?> aClass;
        try {
            aClass = loader.loadClass(className);
        } catch (ClassNotFoundException e) {
            aClass = defineClass(className, generateSub(aSpecClass, SUFFIX), loader);
        }
        // TODO check aSpecClass has only one constructors
        return (T) aClass.getConstructors()[0].newInstance((Object[]) arguments);
    }

    private static byte[] generateSub(Class<?> clazz, String suffix) throws IOException {
        String name = Type.getInternalName(clazz) + ".class";
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new CVisitor(cw);
        ClassReader cr = new ClassReader(clazz.getClassLoader().getResourceAsStream(name));
        cr.accept(cv, ClassReader.SKIP_DEBUG);
        return cw.toByteArray();
    }

    private static Class<?> defineClass(String subClassName, byte[] bytecode, ClassLoader loader) throws Exception {
        Object[] args = {subClassName, bytecode, Integer.valueOf(0), Integer.valueOf(bytecode.length)};
        Class<?>[] parameterTypes = {String.class, byte[].class, int.class, int.class};
        Method method = ClassLoader.class.getDeclaredMethod("defineClass", parameterTypes);
        method.setAccessible(true);
        return (Class<?>) method.invoke(loader, args);
    }

    private static final String SUFFIX = "Stompized";

    private Stompize() {}

    private static class CVisitor extends ClassVisitor {

        public CVisitor(ClassWriter cw) { super(ASM4, cw); }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            parent = name;
            self = name + SUFFIX;
            super.visit(version, ACC_PUBLIC + ACC_SUPER, self, null, name, null);
        }

        @Override
        public void visitSource(String source, String debug) { /*NOOP*/ }

        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            return null;
        }

        @Override
        public MethodVisitor visitMethod(int access, final String name, final String desc, String signature, final String[] exceptions) {
            if ("<clinit>".equals(name)) return null;

            final Type[] argumentTypes = Type.getArgumentTypes(desc);

            if ("<init>".equals(name)) {
                MethodVisitor mv = super.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);
                if (mv != null) visitInit(name, desc, argumentTypes, mv);
                return null;
            }

            if ((access & ACC_ABSTRACT) == ACC_ABSTRACT) {
                MethodVisitor mv = super.visitMethod(access & ~ACC_ABSTRACT, name, desc, signature, exceptions);
                return mv == null ? null : new AMVisitor(mv, argumentTypes, name);
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

        public static final int AUTO_STACK  = 0;
        public static final int AUTO_LOCALS = 0;

        private String parent;
        private String self;

        private class AMVisitor extends MethodVisitor {
            public AMVisitor(MethodVisitor mv, Type[] argumentTypes, String name) {
                super(ASM4, mv);
                this.argumentTypes = argumentTypes;
                outputIndex = argumentTypes.length + 1;
                command = name.toUpperCase();
                annotationDescs = new ArrayList<String>();
                annotationValues = new ArrayList<String>();
            }

            @Override
            public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
                AnnotationVisitor av = super.visitParameterAnnotation(parameter, desc, visible);
                annotationDescs.add(desc);
                return new AnnotationVisitor(ASM4, av) {
                    @Override
                    public void visit(String name, Object value) {
                        annotationValues.add(value.toString());
                        super.visit(name, value);
                    }
                };
            }

            @Override
            public void visitEnd() {
                visitCode();

                visitVarInsn(ALOAD, 0);
                visitMethodInsn(INVOKEVIRTUAL, self, "output", "()Lcom/github/zhongl/stompize/FrameVisitor;");
                visitVarInsn(ASTORE, outputIndex);

                visitVarInsn(ALOAD, outputIndex);
                visitLdcInsn(command);
                visitMethodInsn(INVOKESTATIC, "com/github/zhongl/stompize/Bytes", "bytes", "(Ljava/lang/String;)[B");
                visitMethodInsn(INVOKEINTERFACE, "com/github/zhongl/stompize/FrameVisitor", "command", "([B)V");

                for (int i = 0; i < annotationValues.size(); i++) {
                    String value = annotationValues.get(i);
                    visitVarInsn(ALOAD, outputIndex);
                    visitLdcInsn(value);
                    visitMethodInsn(INVOKESTATIC, "com/github/zhongl/stompize/Bytes", "bytes", "(Ljava/lang/String;)[B");
                    visitVarInsn(ALOAD, i + 1);

                    String method = Type.getDescriptor(Required.class).equals(annotationDescs.get(i)) ? "required" : "optional";
                    visitMethodInsn(INVOKEINTERFACE, "com/github/zhongl/stompize/FrameVisitor", method, "([BLjava/lang/Object;)V");
                }

                if (argumentTypes.length - annotationDescs.size() == 1) {
                    // only last one argument without annotation supposed to be a content.
                    // TODO check content type should not be primary
                    visitVarInsn(ALOAD, outputIndex);
                    visitVarInsn(ALOAD, argumentTypes.length);
                    visitMethodInsn(INVOKEINTERFACE, "com/github/zhongl/stompize/FrameVisitor", "content", "(Ljava/lang/Object;)V");
                }

                visitInsn(RETURN);
                visitMaxs(AUTO_STACK, AUTO_LOCALS);
                super.visitEnd();
            }

            final int          outputIndex;
            final String       command;
            final List<String> annotationDescs;
            final List<String> annotationValues;
            final Type[]       argumentTypes;
        }
    }
}
