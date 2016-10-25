package com.github.drxaos.edu.instrumentation.generator;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.io.PrintStream;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;

//  Generating class HelloWorld with code:
//  public class HelloWorld {
//      public static void main (String[] args) {
//          System.out.println("Hello world!");
//      }
//  }
public class HelloWorldClassGenerator {

    public static final String HELLO_WORLD_CLASS_NAME = "HelloWorld";

    public static void main(String[] args) throws Exception {
        Class<?> cls1 = generateHelloWorldClassViaGeneratorAdapter("11111");
        cls1.getMethod("main", new Class[]{String[].class}).invoke(null, new Object[]{null});

        Class<?> cls2 = generateHelloWorldClassViaMethodVisitor("22222");
        cls2.getMethod("main", new Class[]{String[].class}).invoke(null, new Object[]{null});
    }

    public static Class<?> generateHelloWorldClassViaMethodVisitor(String message) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classWriter.visit(V1_6,
                ACC_PUBLIC,
                HELLO_WORLD_CLASS_NAME,
                null,
                getInternalName(Object.class),
                null);

        MethodVisitor constructor = classWriter.visitMethod(
                ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null);
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, getInternalName(Object.class), "<init>", "()V");
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();

        MethodVisitor main = classWriter.visitMethod(
                ACC_PUBLIC + ACC_STATIC,
                "main",
                "([Ljava/lang/String;)V",
                null,
                null
        );

        main.visitFieldInsn(GETSTATIC, getInternalName(System.class), "out", getDescriptor(PrintStream.class));
        main.visitLdcInsn(message);
        main.visitMethodInsn(INVOKEVIRTUAL, getInternalName(PrintStream.class), "println", "(Ljava/lang/String;)V");
        main.visitInsn(RETURN);
        main.visitMaxs(2, 2);
        main.visitEnd();

        return ASMUtilities.defineClass(HELLO_WORLD_CLASS_NAME, classWriter.toByteArray());
    }

    public static Class<?> generateHelloWorldClassViaGeneratorAdapter(String message) {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classWriter.visit(V1_6, ACC_PUBLIC, HELLO_WORLD_CLASS_NAME, null, getInternalName(Object.class), null);

        Method constructorMethod = Method.getMethod("void <init> ()");
        GeneratorAdapter constructor = new GeneratorAdapter(ACC_PUBLIC, constructorMethod, null, null, classWriter);
        constructor.loadThis();
        constructor.invokeConstructor(Type.getType(Object.class), constructorMethod);
        constructor.returnValue();
        constructor.endMethod();

        Method mainMethod = Method.getMethod("void main (String[])");
        GeneratorAdapter main = new GeneratorAdapter(ACC_PUBLIC + ACC_STATIC, mainMethod, null, null, classWriter);
        main.getStatic(Type.getType(System.class), "out", Type.getType(PrintStream.class));
        main.push(message);
        main.invokeVirtual(Type.getType(PrintStream.class), Method.getMethod("void println (String)"));
        main.returnValue();
        main.endMethod();

        return ASMUtilities.defineClass(HELLO_WORLD_CLASS_NAME, classWriter);
    }
}

class ASMUtilities {

    public static Class<?> defineClass(String name, byte[] bytes) {
        return new MyClassLoader(MyClassLoader.class.getClassLoader()).defineClassForName(name, bytes);
    }

    public static Class<?> defineClass(String name, ClassWriter writer) {
        return defineClass(name, writer.toByteArray());
    }
}

class MyClassLoader extends ClassLoader {

    public MyClassLoader(ClassLoader classLoader) {
        super(classLoader);
    }

    public Class<?> defineClassForName(String name, byte[] data) {
        return this.defineClass(name, data, 0, data.length);
    }

}