package com.github.zhongl.stompize;

import java.io.IOException;
import java.lang.reflect.Method;
import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ASM4;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
abstract class GenerateSubClass<T> extends ClassVisitor {

    GenerateSubClass(Class<? extends T> aClass, String suffix) {
        super(ASM4, new ClassWriter(ClassWriter.COMPUTE_MAXS));
        this.aClass = aClass;
        this.suffix = suffix;
    }

    @Override
    public final void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        parent = name;
        self = name + suffix;
        super.visit(version, ACC_PUBLIC, self, signature, name, interfaces());
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return null;
    }

    public byte[] toByteArray() throws IOException {
        visit(aClass, this);
        return ((ClassWriter) cv).toByteArray();
    }

    @SuppressWarnings("unchecked")
    public Class<T> apply() throws Exception {
        byte[] bytecode = toByteArray();
        Object[] args = {aClass.getName() + suffix, bytecode, Integer.valueOf(0), Integer.valueOf(bytecode.length)};
        Class<?>[] parameterTypes = {String.class, byte[].class, int.class, int.class};
        Method method = ClassLoader.class.getDeclaredMethod("defineClass", parameterTypes);
        method.setAccessible(true);
        return (Class<T>) method.invoke(aClass.getClassLoader(), args);

    }

    protected static void visit(Class<?> c, ClassVisitor cv) {
        try {
            String name = Type.getInternalName(c) + ".class";
            ClassReader cr = new ClassReader(c.getClassLoader().getResourceAsStream(name));
            cr.accept(cv, ClassReader.SKIP_DEBUG);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected static final String[] NULL = new String[0];

    protected String[] interfaces() { return NULL; }

    protected final Class<? extends T> aClass;
    protected final String             suffix;
    protected       String             parent;
    protected       String             self;

}
