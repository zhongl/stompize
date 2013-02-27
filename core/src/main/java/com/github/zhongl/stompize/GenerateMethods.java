package com.github.zhongl.stompize;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

import static org.objectweb.asm.Opcodes.ASM4;

/** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
abstract class GenerateMethods extends ClassVisitor {
    protected GenerateMethods(ClassVisitor cv) {super(ASM4, cv);}

    @Override
    final public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) { }

    @Override
    final public void visitSource(String source, String debug) { }

    @Override
    final public void visitOuterClass(String owner, String name, String desc) { }

    @Override
    final public AnnotationVisitor visitAnnotation(String desc, boolean visible) { return null; }

    @Override
    final public void visitAttribute(Attribute attr) { }

    @Override
    final public void visitInnerClass(String name, String outerName, String innerName, int access) {}

    @Override
    final public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return null;
    }

    @Override
    final public void visitEnd() { }
}
