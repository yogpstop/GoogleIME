package com.yogpc.gi.asm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class AsmFixer extends ClassLoader {
  private static final class MyRemapper extends Remapper {
    public MyRemapper() {}

    @Override
    public String map(final String typeName) {
      if ("org/objectweb/asm/commons/RemappingClassAdapter".equals(typeName))
        return "com/yogpc/fb/fix/RemappingClassAdapter";
      if ("org/objectweb/asm/commons/RemappingMethodAdapter".equals(typeName))
        return "com/yogpc/fb/fix/RemappingMethodAdapter";
      if ("org/objectweb/asm/commons/LocalVariablesSorter".equals(typeName))
        return "org/objectweb/asm/MethodVisitor";
      return typeName;
    }
  }

  private static final Remapper rmp = new MyRemapper();
  private static final AsmFixer fixer = new AsmFixer();
  private final Constructor<? extends ClassVisitor> con;

  private static final void fixConstructor(final ClassNode cn) {
    for (final MethodNode mn : cn.methods)
      if ("<init>".equals(mn.name)
          && "(IILjava/lang/String;Lorg/objectweb/asm/MethodVisitor;Lorg/objectweb/asm/commons/Remapper;)V"
              .equals(mn.desc)) {
        AbstractInsnNode ain;
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain.getOpcode() == Opcodes.ILOAD && ((VarInsnNode) ain).var == 2
              || ain.getOpcode() == Opcodes.ALOAD && ((VarInsnNode) ain).var == 3) {
            ain = ain.getPrevious();
            mn.instructions.remove(ain.getNext());
          } else if (ain.getOpcode() == Opcodes.INVOKESPECIAL) {
            final MethodInsnNode min = (MethodInsnNode) ain;
            if (min.owner.equals("org/objectweb/asm/MethodVisitor") && min.name.equals("<init>")
                && min.desc.equals("(IILjava/lang/String;Lorg/objectweb/asm/MethodVisitor;)V"))
              min.desc = "(ILorg/objectweb/asm/MethodVisitor;)V";
          }
        break;
      }
  }

  private static final void fixRemapEntries(final ClassNode cn) {
    for (final MethodNode mn : cn.methods)
      if ("remapEntries".equals(mn.name)
          && "(I[Ljava/lang/Object;)[Ljava/lang/Object;".equals(mn.desc)) {
        final LabelNode ln = new LabelNode();
        AbstractInsnNode ain;
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (!(ain instanceof LabelNode)) {
            mn.instructions.insertBefore(ain, new VarInsnNode(Opcodes.ALOAD, 2));
            mn.instructions.insertBefore(ain, new JumpInsnNode(Opcodes.IFNONNULL, ln));
            mn.instructions.insertBefore(ain, new VarInsnNode(Opcodes.ALOAD, 2));
            mn.instructions.insertBefore(ain, new InsnNode(Opcodes.ARETURN));
            mn.instructions.insertBefore(ain, ln);
            break;
          }
        break;
      }
  }

  private static final ClassNode get(final String name) throws IOException {
    final InputStream is = ClassVisitor.class.getClassLoader().getResource(name).openStream();
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final byte[] byteChunk = new byte[4096];
    int n;
    while ((n = is.read(byteChunk)) >= 0)
      baos.write(byteChunk, 0, n);
    is.close();
    final ClassReader cr = new ClassReader(baos.toByteArray());
    final ClassNode cn = new ClassNode();
    final RemappingClassAdapter rca = new RemappingClassAdapter(cn, rmp);
    cr.accept(rca, ClassReader.EXPAND_FRAMES);
    return cn;
  }

  @SuppressWarnings("unchecked")
  private final Constructor<? extends ClassVisitor> write(final ClassNode cn) {
    final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    cn.accept(cw);
    final byte[] ba = cw.toByteArray();
    try {
      return (Constructor<? extends ClassVisitor>) defineClass(cn.name.replace('/', '.'), ba, 0,
          ba.length, null).getConstructor(ClassVisitor.class, Remapper.class);
    } catch (final NoSuchMethodException e) {
      return null;
    }
  }

  public AsmFixer() {
    super();
    ClassNode rca = null, rma = null;
    try {
      rca = get("org/objectweb/asm/commons/RemappingClassAdapter.class");
      rma = get("org/objectweb/asm/commons/RemappingMethodAdapter.class");
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    fixConstructor(rma);
    fixRemapEntries(rma);
    write(rma);
    this.con = write(rca);
  }

  public static ClassVisitor InitAdapter(final ClassVisitor cv, final Remapper rem)
      throws Exception {
    return fixer.con.newInstance(cv, rem);
  }
}
