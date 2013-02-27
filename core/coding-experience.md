# Clean Code

- 降低代码重复的终极方法莫过于用代码生成代码, 但要看代价, 如:
    - 是否易于编写
    - 是否易于测试
    - 是否易于理解

# ASM

- 参考样例代码的`ASMified Code`让`ASM`的编码更轻松
- 动态生成的字节码没有对应的源码, 因此一些`visitXXX`可以忽略:
    - ClassWriter.visitSource
    - MethodVisitor.visitLabel, 注意, 分支/循环/异常的情况不能忽略
    - MethodVisitor.visitLineNumber
    - MethodVisitor.visitLocalVariable
- 字节码中关于类和方法的名称及描述, 可以通过`org.objectweb.asm.Type`的静态工具方法方便的转换得到
- 分别使用ASMified和TraceClassVisitor输出结果进行diff来诊断字节码生成上的bug.
- 数组对象的Type转对应的Class, 有点绕:

    if (t.getSort() == Type.ARRAY) {
        Type elementType = t.getElementType();
        Class<?> c = aClass.getClassLoader().loadClass(elementType.getClassName());
        return Array.newInstance(c, 0).getClass();
    }


# Codec

- 编解码代码实现中, 相比数组这样的基本数据结构而言, 创建和填充领域对象可能成为性能瓶颈