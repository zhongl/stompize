package com.github.zhongl.stompize;

import static java.lang.reflect.Modifier.isAbstract;

import com.google.common.base.Throwables;
import java.lang.reflect.Method;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static com.github.zhongl.stompize.Stompize.isCommandMethod;
import static org.objectweb.asm.Opcodes.*;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
class GenerateStompizebleApply<T> extends GenerateMethods {
    public GenerateStompizebleApply(ClassVisitor cv, Class<T> specification, Class<? extends T> aClass, String self) {
        super(cv);
        this.specification = specification;
        this.aClass = aClass;
        this.self = self;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (!"apply".equals(name)) return null;

        final MethodVisitor mv = super.visitMethod(ACC_PUBLIC, name, desc, signature, exceptions);
        mv.visitCode();

        new ForeachOverrideCommandGenerateCallbackBranch(mv).apply();

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0); // auto compute stack and locals
        mv.visitEnd();
        return null;
    }

    /** @see Stompizeble#apply(String, Map, Object) */
    private class ForeachOverrideCommandGenerateCallbackBranch extends ForeachFrameOf {

        public ForeachOverrideCommandGenerateCallbackBranch(MethodVisitor mv) {
            super(aClass);
            this.mv = mv;
        }

        @Override
        protected boolean exclude(Method method) {
            return isAbstract(method.getModifiers()) || !isCommandMethod(method.getName(), method.getParameterTypes(), specification);
        }

        @Override
        protected void command(String name, Method method) {
            this.method = method;
            label = new Label();

            mv.visitLdcInsn(name.toUpperCase());
            mv.visitVarInsn(ALOAD, COMMAND_INDEX);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
            mv.visitJumpInsn(IFEQ, label);

            mv.visitVarInsn(ALOAD, THIS_INDEX);
        }

        @Override
        protected void required(String headerName, int index) {
            Class<?> c = method.getParameterTypes()[index - 1];
            try {
                c.getConstructor(String.class);
            } catch (NoSuchMethodException e) {
                throw new StompizeException(e.toString());
            }
            String internalName = Type.getInternalName(c);
            mv.visitTypeInsn(NEW, internalName);
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, HEADERS_INDEX);
            mv.visitLdcInsn(headerName);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "remove", "(Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            mv.visitMethodInsn(INVOKESPECIAL, internalName, "<init>", "(Ljava/lang/String;)V");
        }

        @Override
        protected void content(int index) {
            if (index < 1) return;

            newContentObject(method.getParameterTypes()[index - 1]);
        }

        @Override
        protected void optionals(int index) {
            Class<? extends Header>[] optionalClasses = getOptionalClasses();
            if (optionalClasses.length == 0) return;

            mv.visitVarInsn(ALOAD, THIS_INDEX);
            loadInt(optionalClasses.length);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");

            for (int i = 0; i < optionalClasses.length; i++) {
                Class<? extends Header> c = optionalClasses[i];
                mv.visitInsn(DUP);
                loadInt(i);
                mv.visitLdcInsn(Type.getType(c));
                mv.visitInsn(AASTORE);
            }

            mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "asList", "([Ljava/lang/Object;)Ljava/util/List;");
            mv.visitVarInsn(ALOAD, HEADERS_INDEX);
            mv.visitMethodInsn(INVOKEVIRTUAL, self, "toOptionals", "(Ljava/lang/Iterable;Ljava/util/Map;)[Lcom/github/zhongl/stompize/Header;");
        }

        @Override
        protected void end() {
            mv.visitMethodInsn(INVOKEVIRTUAL, self, method.getName(), Type.getMethodDescriptor(method));
            mv.visitInsn(RETURN);
            mv.visitLabel(label);
            mv.visitFrame(F_SAME, 0, null, 0, null);
        }

        private Class<? extends Header>[] getOptionalClasses() {
            try {
                return specification.getMethod(method.getName(), method.getParameterTypes()).getAnnotation(Command.class).optionals();
            } catch (NoSuchMethodException e) {
                throw Throwables.propagate(e);
            }
        }

        private void loadInt(int i) {
            switch (i) {
                case 0:
                    mv.visitInsn(ICONST_0);
                    break;
                case 1:
                    mv.visitInsn(ICONST_1);
                    break;
                case 2:
                    mv.visitInsn(ICONST_2);
                    break;
                case 3:
                    mv.visitInsn(ICONST_3);
                    break;
                case 4:
                    mv.visitInsn(ICONST_4);
                    break;
                case 5:
                    mv.visitInsn(ICONST_5);
                    break;
                default:
                    mv.visitIntInsn(BIPUSH, i);
            }
        }

        private void newContentObject(Class<?> c) {
            // Content class should have a constructor with Object parameter.
            try {
                c.getConstructor(Object.class, String.class);
            } catch (NoSuchMethodException e) {
                throw new StompizeException(e.toString());
            }
            String internalName = Type.getInternalName(c);
            mv.visitTypeInsn(NEW, internalName);
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, CONTENT_INDEX);
            mv.visitVarInsn(ALOAD, HEADERS_INDEX);
            mv.visitLdcInsn("content-type");
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "remove", "(Ljava/lang/Object;)Ljava/lang/Object;");
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            mv.visitMethodInsn(INVOKESPECIAL, internalName, "<init>", "(Ljava/lang/Object;Ljava/lang/String;)V");
        }

        static final int THIS_INDEX = 0;

        static final int COMMAND_INDEX = 1; // String
        static final int HEADERS_INDEX = 2; // Map
        static final int CONTENT_INDEX = 3; // Object
        private final MethodVisitor mv;

        Method method;

        Label label;
    }

    private final Class<T>           specification;
    private final Class<? extends T> aClass;
    private final String             self;
}
