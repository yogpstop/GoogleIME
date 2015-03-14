package com.yogpc.gi.asm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class Analyzer {
  private static final <K> void add1d(final Map<K, Integer> m, final K k) {
    Integer m2 = m.get(k);
    if (m2 == null)
      m2 = Integer.valueOf(1);
    else
      m2 = Integer.valueOf(m2.intValue() + 1);
    m.put(k, m2);
  }

  private static final <K, L> void add2d(final Map<K, Map<L, Integer>> m, final K k1, final L k2) {
    Map<L, Integer> m1 = m.get(k1);
    if (m1 == null)
      m.put(k1, m1 = new HashMap<L, Integer>());
    add1d(m1, k2);
  }

  private static void guiTextField(final ClassNode cn) {
    final Map<String, Map<String, Integer>> members = new HashMap<String, Map<String, Integer>>();// DESC-NAME-COUNT
    final Map<String, Integer> trefs = new HashMap<String, Integer>();// FN=FD|MNMD-COUNT
    for (final MethodNode mn : cn.methods) {
      AbstractInsnNode ain;
      for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
        if (ain instanceof MethodInsnNode) {
          final MethodInsnNode min = (MethodInsnNode) ain;
          if (min.owner.equals(cn.name)) {
            add2d(members, min.desc, min.name);
            add1d(trefs, min.name + min.desc + "|" + mn.name + mn.desc);
          }
        } else if (ain instanceof FieldInsnNode) {
          final FieldInsnNode fin = (FieldInsnNode) ain;
          if (fin.owner.equals(cn.name)) {
            add2d(members, fin.desc, fin.name);
            add1d(trefs, fin.name + "=" + fin.desc + "|" + mn.name + mn.desc);
          }
        }
    }
    // getText, setText
    final Map<String, Integer> v_str = members.get("()Ljava/lang/String;");
    final Map<String, Integer> str_v = members.get("(Ljava/lang/String;)V");
    for (final MethodNode mn : cn.methods)
      if ("()Ljava/lang/String;".equals(mn.desc)) {
        final Integer i = v_str.get(mn.name);
        if (i == null)
          Mapping.addM("GuiTextField", "getText", mn.name);
      } else if ("(Ljava/lang/String;)V".equals(mn.desc)) {
        final Integer i = str_v.get(mn.name);
        if (i == null)
          Mapping.addM("GuiTextField", "setText", mn.name);
      }
    // setCursorPosition
    String setcpos = null;
    for (final Map.Entry<String, Integer> e : trefs.entrySet()) {
      final int j = e.getKey().indexOf("(I)V|");
      if (0 <= j && j < e.getKey().indexOf("()V") && e.getValue().intValue() == 1)
        // twice call is normality
        setcpos =
            Mapping.addM("GuiTextField", "setCursorPosition",
                e.getKey().substring(0, e.getKey().indexOf('(')));
    }
    // getCursorPosition
    String tcp = null;
    for (final Map.Entry<String, Integer> e : trefs.entrySet())
      if (0 <= e.getKey().indexOf("=I|" + setcpos + "(I)V"))
        tcp = e.getKey().substring(0, e.getKey().indexOf('='));
    for (final Map.Entry<String, Integer> e : trefs.entrySet()) {
      final int i = e.getKey().indexOf(tcp + "=I|");
      if (0 <= i && i < e.getKey().indexOf("()I"))
        Mapping.addM("GuiTextField", "getCursorPosition",
            e.getKey().substring(e.getKey().indexOf('|') + 1, e.getKey().indexOf('(')));
    }
  }

  private static void fontRenderer(final ClassNode cn) {
    for (final MethodNode mn : cn.methods)
      if ("(C)I".equals(mn.desc)) {
        AbstractInsnNode ain;
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain.getOpcode() == Opcodes.ICONST_M1) {
            Mapping.addM("FontRenderer", "getCharWidth", mn.name);
            break;
          }
      }
  }

  private static void tessellator(final ClassNode cn) {
    for (final MethodNode mn : cn.methods)
      if (("()L" + cn.name + ';').equals(mn.desc) && (Opcodes.ACC_STATIC & mn.access) != 0)
        Mapping.addM("Tessellator", "getInstance", mn.name);
      else if ("()I".equals(mn.desc))
        Mapping.addM("Tessellator", "draw", mn.name);
      // TODO may not working on older than 1.7.10
      else if (mn.desc.startsWith("()L"))
        Mapping.addM("Tessellator", "getWorldRenderer", mn.name);
  }

  private static void worldRenderer(final ClassNode cn) {
    AbstractInsnNode ain;
    for (final MethodNode mn : cn.methods)
      if ("(DDD)V".equals(mn.desc)) {
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain instanceof MethodInsnNode
              && ((MethodInsnNode) ain).owner.equals("java/util/Iterator")) {
            Mapping.addM("WorldRenderer", "addVertex", mn.name);
            break;
          }
      } else if ("()V".equals(mn.desc))
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain instanceof IntInsnNode && ((IntInsnNode) ain).operand == 7) {
            Mapping.addM("WorldRenderer", "startDrawingQuads", mn.name);
            break;
          }
  }

  private static void analyze(final byte[] ba) {
    final ClassNode cn = new ClassNode();
    final ClassReader cr = new ClassReader(ba);
    cr.accept(cn, ClassReader.EXPAND_FRAMES);
    boolean fr = false, wr = false, gtf = false;
    int tes = 0;
    for (final MethodNode mn : cn.methods)
      if ("(IIZ)I".equals(mn.desc)) {
        gtf = true;
        Mapping.addC("GuiTextField", cn.name);
      } else if ("(ICZ)F".equals(mn.desc)) {
        fr = true;
        Mapping.addC("FontRenderer", cn.name);
      } else if ("(DDDDD)V".equals(mn.desc) && !cn.name.contains("realms")) {// TODO class name
        wr = true;
        Mapping.addC("WorldRenderer", cn.name);
      } else if (("()L" + cn.name + ";").equals(mn.desc) && (mn.access & Opcodes.ACC_STATIC) != 0)
        tes |= 1;
      else if ("<init>".equals(mn.name) && "(I)V".equals(mn.desc))
        tes |= 2;
      else if ("()I".equals(mn.desc))
        tes |= 4;
    if (tes == 7 && cn.methods.size() == 5) {
      // TODO before than 1.8
      Mapping.addC("Tessellator", cn.name);
      tessellator(cn);
    } else if (gtf)
      guiTextField(cn);
    else if (fr)
      fontRenderer(cn);
    else if (wr)
      worldRenderer(cn);
  }

  public static void anis(final URL url) throws IOException {
    ZipEntry ze;
    final InputStream is = url.openStream();
    final ZipInputStream in = new ZipInputStream(is);
    while ((ze = in.getNextEntry()) != null) {
      if (ze.getName().toLowerCase().endsWith(".class"))
        analyze(jar_entry(in, ze.getSize()));
      in.closeEntry();
    }
    in.close();
    is.close();
  }

  public static byte[] jar_entry(final InputStream in, final long size) throws IOException {
    byte[] data;
    if (size > 0) {
      data = new byte[(int) size];
      int offset = 0;
      do
        offset += in.read(data, offset, data.length - offset);
      while (offset < data.length);
    } else {
      final ByteArrayOutputStream dataout = new ByteArrayOutputStream();
      data = new byte[4096];
      int len;
      while ((len = in.read(data)) != -1)
        dataout.write(data, 0, len);
      data = dataout.toByteArray();
    }
    return data;
  }

  public static void main(final String[] arg) throws IOException {
    ZipEntry ze;
    final InputStream is = new FileInputStream(new File(arg[0]));
    final ZipInputStream in = new ZipInputStream(is);
    while ((ze = in.getNextEntry()) != null) {
      if (ze.getName().toLowerCase().endsWith(".class"))
        analyze(jar_entry(in, ze.getSize()));
      in.closeEntry();
    }
    in.close();
    is.close();
  }
}
