package com.yogpc.mi.asm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

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
        boolean found = false;
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain instanceof LdcInsnNode && ((LdcInsnNode) ain).cst instanceof String
              && ((String) ((LdcInsnNode) ain).cst).equals("0123456789abcdef"))
            found = true;
        if (!found)
          Mapping.addM("FontRenderer", "getCharWidth", mn.name);
      } else if ("(Ljava/lang/String;III)I".equals(mn.desc)) {
        AbstractInsnNode ain;
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain.getOpcode() == Opcodes.ICONST_0)
            Mapping.addM("FontRenderer", "drawString", mn.name);
      }
  }

  private static void minecraft(final ClassNode cn) {
    final List<String> map = new ArrayList<String>();
    for (final MethodNode mn : cn.methods)
      if ("(II)V".equals(mn.desc)) {
        AbstractInsnNode ain;
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain instanceof FieldInsnNode && ((FieldInsnNode) ain).owner.equals(cn.name)
              && ((FieldInsnNode) ain).desc.startsWith("L"))
            map.add("(" + ((FieldInsnNode) ain).desc + ")V");
      }
    for (final MethodNode mn : cn.methods)
      if ((mn.access & Opcodes.ACC_STATIC) != 0 && mn.desc.equals("()L" + cn.name + ";"))
        Mapping.addM("Minecraft", "getMinecraft", mn.name);
      else if (map.contains(mn.desc)) {
        int phase = -1;
        AbstractInsnNode ain;
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain instanceof VarInsnNode)
            phase = ((VarInsnNode) ain).var;
          else if (phase == 1 && ain.getOpcode() == Opcodes.PUTFIELD)
            Mapping.addM("Minecraft", "currentScreen", ((FieldInsnNode) ain).name);
          else
            phase = -1;
      } else if ("()V".equals(mn.desc)) {
        int phase = 0;
        AbstractInsnNode ain;
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain instanceof LdcInsnNode && ((LdcInsnNode) ain).cst instanceof String
              && ((String) ((LdcInsnNode) ain).cst).contains("/font/")
              && !((String) ((LdcInsnNode) ain).cst).contains("alternate")
              && !((String) ((LdcInsnNode) ain).cst).contains("sga"))
            phase = 12;
          else if (phase > 0 && ain.getOpcode() == Opcodes.INVOKESPECIAL
              && ((MethodInsnNode) ain).desc.endsWith("Z)V"))
            phase = -4;
          else if (phase < 0 && ain.getOpcode() == Opcodes.PUTFIELD
              && ((FieldInsnNode) ain).owner.equals(cn.name))
            Mapping.addM("Minecraft", "fontRenderer", ((FieldInsnNode) ain).name);
          else if (phase > 0)
            phase--;
          else if (phase < 0)
            phase++;

      }
  }

  private static void guiScreen(final ClassNode cn) {
    for (final MethodNode mn : cn.methods) {
      if (!mn.desc.startsWith("(L") || !mn.desc.endsWith(";II)V"))
        continue;
      if (mn.desc.substring(2, mn.desc.length() - 5).contains(";"))
        continue;
      int phase = -1;
      AbstractInsnNode ain;
      boolean swar = false;
      for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
        if (ain instanceof VarInsnNode)
          phase = ((VarInsnNode) ain).var;
        else if (phase == 1 && ain.getOpcode() == Opcodes.GETFIELD) {
          swar = true;
          phase = -1;
        } else if (swar && (phase == 2 || phase == 3) && ain.getOpcode() == Opcodes.PUTFIELD) {
          Mapping.addM("GuiScreen", phase == 2 ? "width" : "height", ((FieldInsnNode) ain).name);
          phase = -1;
        } else
          phase = -1;
    }
  }

  private static boolean isKeyHook(final MethodNode mn) {
    AbstractInsnNode ain;
    for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
      if (ain.getOpcode() == Opcodes.INVOKESTATIC
          && "org/lwjgl/input/Keyboard".equals(((MethodInsnNode) ain).owner)
          && "getEventKeyState".equals(((MethodInsnNode) ain).name)
          && "()Z".equals(((MethodInsnNode) ain).desc))
        return true;
    return false;
  }

  private static void guiScreenBook(final ClassNode cn) {
    for (final MethodNode mn : cn.methods)
      if ("(Ljava/lang/String;)V".equals(mn.desc)) {
        AbstractInsnNode ain;
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain.getOpcode() == Opcodes.BIPUSH && ((IntInsnNode) ain).operand == 118)
            Mapping.addM("GuiScreenBook", "pageInsertIntoCurrent", mn.name);
      }
    for (final FieldNode fn : cn.fields)
      if ("Ljava/lang/String;".equals(fn.desc))
        Mapping.addM("GuiScreenBook", "bookTitle", fn.name);
  }

  private static void guiEditSign(final ClassNode cn) {
    AbstractInsnNode ain;
    for (final MethodNode mn : cn.methods)
      if ("<init>".equals(mn.name)) {
        int phase = 0;
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (phase == 0 && ain.getOpcode() == Opcodes.ALOAD)
            phase = 1;
          else if (phase == 1 && ain.getOpcode() == Opcodes.PUTFIELD) {
            Mapping.addM("GuiEditSign", "tile", ((FieldInsnNode) ain).name);
            break;
          }
      } else if ("(CI)V".equals(mn.desc))
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain.getOpcode() == Opcodes.PUTFIELD && ((FieldInsnNode) ain).owner.equals(cn.name)
              && ((FieldInsnNode) ain).desc.equals("I")) {
            Mapping.addM("GuiEditSign", "line", ((FieldInsnNode) ain).name);
            break;
          }
  }

  private static void tileEntitySign(final ClassNode cn) {
    for (final FieldNode fn : cn.fields)
      if (fn.desc.startsWith("[")) {
        Mapping.addM("TileEntitySign", "signText", fn.name);
        Mapping.addC("IChatComponent", fn.desc.substring(2, fn.desc.length() - 1));
        break;
      }
  }

  private static void chatComponentStyle(final ClassNode cn) {
    int i = Integer.MAX_VALUE;
    String n = null;
    for (final MethodNode mn : cn.methods)
      if ("()Ljava/lang/String;".equals(mn.desc) && mn.instructions.size() < i
          && !"toString".equals(mn.name)) {
        i = mn.instructions.size();
        n = mn.name;
      }
    Mapping.addM("IChatComponent", "getUnformattedText", n);
  }

  private static void analyze(final byte[] ba) {
    final ClassNode cn = new ClassNode();
    final ClassReader cr = new ClassReader(ba);
    cr.accept(cn, ClassReader.EXPAND_FRAMES);
    boolean fr = false, gtf = false, mc = false, gs = false, gsb = false, ges = false, tes = false, ccs =
        false;
    for (final MethodNode mn : cn.methods)
      // TODO Obfuscated detection
      if ("(IIZ)I".equals(mn.desc) && cn.name.indexOf('/') < 0) {
        gtf = true;
        Mapping.addC("GuiTextField", cn.name);
        // TODO Obfuscated detection
      } else if ("(C)I".equals(mn.desc) && cn.name.indexOf('/') < 0) {
        fr = true;
        Mapping.addC("FontRenderer", cn.name);
      } else if (Asm.findLDC(mn, "########## GL ERROR ##########")) {
        mc = true;
        Mapping.addC("Minecraft", cn.name);
        // TODO Obfuscated detection
      } else if ("<init>".equals(mn.name) && Asm.findLDC(mn, "pages") && cn.name.indexOf('/') < 0) {
        gsb = true;
        Mapping.addC("GuiScreenBook", cn.name);
      } else if (Asm.findLDC(mn, "sign.edit") || Asm.findLDC(mn, "Edit sign message:")) {
        ges = true;
        Mapping.addC("GuiEditSign", cn.name);
      } else if (Asm.findLDC(mn, "Text")) {
        tes = true;
        Mapping.addC("TileEntitySign", cn.name);
      } else if (Asm.startLDC(mn, "BaseComponent"))
        ccs = true;
      else if (Asm.startLDC(mn, "TextComponent"))
        Mapping.addC("ChatComponentText", cn.name);
      // TODO Obfuscated detection
      else if (isKeyHook(mn) && cn.name.indexOf('/') < 0)
        gs = true;
    if (gtf)
      guiTextField(cn);
    else if (gsb)
      guiScreenBook(cn);
    else if (fr)
      fontRenderer(cn);
    else if (mc)
      minecraft(cn);
    else if (ges)
      guiEditSign(cn);
    else if (tes)
      tileEntitySign(cn);
    else if (ccs)
      chatComponentStyle(cn);
    else if (gs) {// always NOT mc
      Mapping.addC("GuiScreen", cn.name);
      guiScreen(cn);
    }
  }

  private static byte[] jar_entry(final InputStream in, final long size) throws IOException {
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

  static void anis(final URL url) throws IOException {
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
}
