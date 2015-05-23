package com.yogpc.mi.asm;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LogWrapper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

@SuppressWarnings("deprecation")
public class Asm implements IClassTransformer {
  private static final String focuse[] = new String[2];

  private static final void key(final MethodNode mn, final String cn) {
    AbstractInsnNode ain;
    for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
      if (ain instanceof FieldInsnNode && "Z".equals(((FieldInsnNode) ain).desc)
          && cn.equals(((FieldInsnNode) ain).owner)) {
        if (((FieldInsnNode) ain).name.equals(focuse[0])
            || ((FieldInsnNode) ain).name.equals(focuse[1]))
          continue;
        if (focuse[0] == null)
          focuse[0] = ((FieldInsnNode) ain).name;
        else if (focuse[1] == null)
          focuse[1] = ((FieldInsnNode) ain).name;
      }
  }

  private static final void init(final MethodNode mn, final String cn) {
    AbstractInsnNode p = mn.instructions.getFirst();
    while (p.getOpcode() != Opcodes.INVOKESPECIAL)
      p = p.getNext();
    LogWrapper.info("[MCIME] GTFHandler.<init> call on %s.%s%s", cn, mn.name, mn.desc);
    mn.instructions.insert(p, new FieldInsnNode(Opcodes.PUTFIELD, cn, "manager",
        "Lcom/yogpc/mi/GTFHandler;"));
    mn.instructions.insert(p, new MethodInsnNode(Opcodes.INVOKESPECIAL, "com/yogpc/mi/GTFHandler",
        "<init>", "(L" + cn + ";)V"));
    mn.instructions.insert(p, new VarInsnNode(Opcodes.ALOAD, 0));
    mn.instructions.insert(p, new InsnNode(Opcodes.DUP));
    mn.instructions.insert(p, new TypeInsnNode(Opcodes.NEW, "com/yogpc/mi/GTFHandler"));
    mn.instructions.insert(p, new VarInsnNode(Opcodes.ALOAD, 0));
  }

  private static final void draw(final MethodNode mn, final String cn, final String fn,
      final String frt, final String frf, final String xpos, final String ypos, final String width,
      final String height) {
    String bg = null;
    AbstractInsnNode ain;
    for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
      if (ain instanceof FieldInsnNode && "Z".equals(((FieldInsnNode) ain).desc)
          && cn.equals(((FieldInsnNode) ain).owner)) {
        if (((FieldInsnNode) ain).name.equals(focuse[0])
            || ((FieldInsnNode) ain).name.equals(focuse[1]))
          continue;
        bg = ((FieldInsnNode) ain).name;
      }
    for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
      if (ain.getOpcode() == Opcodes.RETURN) {
        LogWrapper.info("[MCIME] GTFHandler.hookDraw call on %s.%s%s", cn, mn.name, mn.desc);
        mn.instructions.insertBefore(ain, new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.insertBefore(ain, new FieldInsnNode(Opcodes.GETFIELD, cn, "manager",
            "Lcom/yogpc/mi/GTFHandler;"));
        mn.instructions.insertBefore(ain, new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.insertBefore(ain, new FieldInsnNode(Opcodes.GETFIELD, cn, fn, "I"));
        mn.instructions.insertBefore(ain, new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.insertBefore(ain, new FieldInsnNode(Opcodes.GETFIELD, cn, frf, frt));
        mn.instructions.insertBefore(ain, new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.insertBefore(ain, new FieldInsnNode(Opcodes.GETFIELD, cn, xpos, "I"));
        mn.instructions.insertBefore(ain, new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.insertBefore(ain, new FieldInsnNode(Opcodes.GETFIELD, cn, ypos, "I"));
        mn.instructions.insertBefore(ain, new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.insertBefore(ain, new FieldInsnNode(Opcodes.GETFIELD, cn, width, "I"));
        mn.instructions.insertBefore(ain, new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.insertBefore(ain, new FieldInsnNode(Opcodes.GETFIELD, cn, height, "I"));
        mn.instructions.insertBefore(ain, new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.insertBefore(ain, new FieldInsnNode(Opcodes.GETFIELD, cn, bg, "Z"));
        mn.instructions.insertBefore(ain, new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
            "com/yogpc/mi/GTFHandler", "hookDraw", "(ILjava/lang/Object;IIIIZ)V"));
      }
  }

  private static final void count(final MethodNode mn, final String cn,
      final Map<String, Integer> map) {
    AbstractInsnNode ain;
    for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
      if ((ain.getOpcode() == Opcodes.GETFIELD || ain.getOpcode() == Opcodes.PUTFIELD)
          && cn.equals(((FieldInsnNode) ain).owner) && "I".equals(((FieldInsnNode) ain).desc)) {
        final Integer i = map.get(((FieldInsnNode) ain).name);
        final int j = i == null ? 1 : i.intValue() + 1;
        map.put(((FieldInsnNode) ain).name, Integer.valueOf(j));
      }
  }

  private static final void gtf(final ClassNode cn) {
    cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "manager",
        "Lcom/yogpc/mi/GTFHandler;", null, null));
    final Map<String, Integer> map = new HashMap<String, Integer>();
    for (final MethodNode mnode : cn.methods)
      if ("(I)V".equals(mnode.desc))
        count(mnode, cn.name, map);
    int maxV = 0;
    String maxK = null;
    for (final Map.Entry<String, Integer> e : map.entrySet())
      if (e.getValue().intValue() > maxV) {
        maxV = e.getValue().intValue();
        maxK = e.getKey();
      }
    String frf = null, frt = null, xpos = null, ypos = null, width = null, height = null;
    for (final MethodNode mn : cn.methods)
      if ("(CI)Z".equals(mn.desc))
        key(mn, cn.name);
      else if (mn.name.equals("<init>")) {
        final int shift = mn.desc.charAt(1) == 'L' ? 0 : 1;
        int phase = -1;
        AbstractInsnNode ain;
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain.getOpcode() == Opcodes.ALOAD && ((VarInsnNode) ain).var == 0)
            phase = 0;
          else if (phase == 0 && ain.getOpcode() == Opcodes.ALOAD
              && ((VarInsnNode) ain).var == 1 + shift)
            phase = 9999;
          else if (phase == 0 && ain.getOpcode() == Opcodes.ILOAD)
            phase = ((VarInsnNode) ain).var - shift;
          else if (phase > 0 && ain.getOpcode() == Opcodes.PUTFIELD) {
            switch (phase) {
              case 2:
                xpos = ((FieldInsnNode) ain).name;
                break;
              case 3:
                ypos = ((FieldInsnNode) ain).name;
                break;
              case 4:
                width = ((FieldInsnNode) ain).name;
                break;
              case 5:
                height = ((FieldInsnNode) ain).name;
                break;
              case 9999:
                frf = ((FieldInsnNode) ain).name;
                frt = ((FieldInsnNode) ain).desc;
                break;
            }
            phase = -1;
          } else
            phase = -1;
      }
    for (final MethodNode mnode : cn.methods)
      if ("<init>".equals(mnode.name))
        init(mnode, cn.name);
      // TODO instruction size
      else if ("()V".equals(mnode.desc) && mnode.instructions.size() > 150)
        draw(mnode, cn.name, maxK, frt, frf, xpos, ypos, width, height);
    for (final MethodNode mn : cn.methods) {
      AbstractInsnNode ain;
      for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
        if (ain.getOpcode() == Opcodes.PUTFIELD) {
          final FieldInsnNode min = (FieldInsnNode) ain;
          if (!cn.name.equals(min.owner) || !"Z".equals(min.desc))
            continue;
          if (!min.name.equals(focuse[0]) && !min.name.equals(focuse[1]))
            continue;
          LogWrapper.info("[MCIME] GTFHandler.hookFocuse call on %s.%s%s", cn.name, mn.name,
              mn.desc);
          mn.instructions.insert(ain, new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
              "com/yogpc/mi/GTFHandler", "hookFocuse", "(ZZ)V"));
          mn.instructions.insert(ain, new FieldInsnNode(Opcodes.GETFIELD, cn.name, focuse[1], "Z"));
          mn.instructions.insert(ain, new VarInsnNode(Opcodes.ALOAD, 0));
          mn.instructions.insert(ain, new FieldInsnNode(Opcodes.GETFIELD, cn.name, focuse[0], "Z"));
          mn.instructions.insert(ain, new VarInsnNode(Opcodes.ALOAD, 0));
          mn.instructions.insert(ain, new FieldInsnNode(Opcodes.GETFIELD, cn.name, "manager",
              "Lcom/yogpc/mi/GTFHandler;"));
          mn.instructions.insert(ain, new VarInsnNode(Opcodes.ALOAD, 0));
        }
    }
  }

  private static boolean done = false;
  private static final Set<URL> analyzed = new HashSet<URL>();

  private static final void init() {
    try {
      final URL[] urls = Launch.classLoader.getURLs();
      for (final URL url : urls) {
        if (!analyzed.add(url))
          continue;
        boolean isTarget = false;
        ZipEntry ze;
        final InputStream is = url.openStream();
        final ZipInputStream in = new ZipInputStream(is);
        while ((ze = in.getNextEntry()) != null) {
          if (ze.getName().startsWith("net/minecraft/client/")) {
            isTarget = true;
            break;
          }
          in.closeEntry();
        }
        in.close();
        is.close();
        if (isTarget) {
          Analyzer.anis(url);
          done = true;
        }
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static final void fr(final ClassNode cn) {
    AbstractInsnNode ain;
    for (final MethodNode mn : cn.methods)
      if ("(C)I".equals(mn.desc)) {
        boolean found = false;
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain instanceof LdcInsnNode && ((LdcInsnNode) ain).cst instanceof String
              && ((String) ((LdcInsnNode) ain).cst).equals("0123456789abcdef")) {
            found = true;
            break;
          }
        if (!found)
          for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
            if (ain.getOpcode() == Opcodes.BIPUSH && ((IntInsnNode) ain).operand == 7) {
              LogWrapper.info("[MCIME] 7=>15 on %s.%s%s", cn.name, mn.name, mn.desc);
              ((IntInsnNode) ain).operand = 15;
            }
        // Optifine
      } else if ("(C)F".equals(mn.desc))
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain.getOpcode() == Opcodes.BIPUSH && ((IntInsnNode) ain).operand == 7) {
            LogWrapper.info("[MCIME] 7=>15 on %s.%s%s", cn.name, mn.name, mn.desc);
            ((IntInsnNode) ain).operand = 15;
          }
  }

  private static final ClassNode gtfm(final ClassNode cn) {
    try {
      final ClassNode out = new ClassNode();
      cn.accept(AsmFixer.InitAdapter(out, Mapping.I));
      return out;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void mc(final ClassNode cn) {
    AbstractInsnNode ain;
    final List<String> map = new ArrayList<String>();
    final List<String> bl = new ArrayList<String>();
    parent: for (final MethodNode mn : cn.methods) {
      if ("(II)V".equals(mn.desc))
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain instanceof FieldInsnNode && ((FieldInsnNode) ain).owner.equals(cn.name)
              && ((FieldInsnNode) ain).desc.startsWith("L"))
            map.add("(" + ((FieldInsnNode) ain).desc + ")V");
      if (mn.desc.endsWith(";Ljava/lang/String;)V"))
        continue;
      for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
        if (ain instanceof LdcInsnNode && ((LdcInsnNode) ain).cst instanceof String
            && ((String) ((LdcInsnNode) ain).cst).contains("chunk update"))
          continue parent;
      for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
        if (ain.getOpcode() == Opcodes.GETFIELD && ((FieldInsnNode) ain).owner.equals(cn.name)
            && ((FieldInsnNode) ain).desc.charAt(0) == 'L'
            && !bl.contains(((FieldInsnNode) ain).name))
          bl.add(((FieldInsnNode) ain).name);
    }
    for (final MethodNode mn : cn.methods)
      if ("()V".equals(mn.desc)) {
        boolean isTarget = false;
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain instanceof LdcInsnNode && ((LdcInsnNode) ain).cst instanceof String
              && ((String) ((LdcInsnNode) ain).cst).contains("chunk update"))
            isTarget = true;
        if (isTarget) {
          final Map<String, Integer> m = new HashMap<String, Integer>();
          final Map<String, String> nd = new HashMap<String, String>();
          for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
            if (ain.getOpcode() == Opcodes.GETFIELD && ((FieldInsnNode) ain).owner.equals(cn.name)
                && ((FieldInsnNode) ain).desc.charAt(0) == 'L') {
              Integer i = m.get(((FieldInsnNode) ain).name);
              if (i == null)
                i = Integer.valueOf(1);
              else
                i = Integer.valueOf(i.intValue() + 1);
              m.put(((FieldInsnNode) ain).name, i);
              nd.put(((FieldInsnNode) ain).name,
                  ((FieldInsnNode) ain).desc.substring(1, ((FieldInsnNode) ain).desc.length() - 1));
            }
          String s = null;
          for (final Map.Entry<String, Integer> e : m.entrySet())
            if (e.getValue().intValue() == 1 && !bl.contains(e.getKey()))
              s = e.getKey();
          int phase = -1;
          for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext()) {
            if (ain.getOpcode() == Opcodes.GETFIELD && ((FieldInsnNode) ain).owner.equals(cn.name)
                && ((FieldInsnNode) ain).name.equals(s))
              phase = 0;
            if (phase == 0 && ain.getOpcode() == Opcodes.INVOKEVIRTUAL
                && ((MethodInsnNode) ain).desc.equals("()V")
                && ((MethodInsnNode) ain).owner.equals(nd.get(s))) {
              LogWrapper.info("[MCIME] TFManager.hookDrawGui call on %s.%s%s", cn.name, mn.name,
                  mn.desc);
              mn.instructions.insert(ain, new MethodInsnNode(Opcodes.INVOKESTATIC,
                  "com/yogpc/mi/TFManager", "hookDrawGui", "()V"));
            }
          }
        }
      } else if (map.contains(mn.desc)) {
        final AbstractInsnNode a = mn.instructions.getFirst();
        LogWrapper.info("[MCIME] TFManager.hookShowGui call on %s.%s%s", cn.name, mn.name, mn.desc);
        mn.instructions.insertBefore(a, new VarInsnNode(Opcodes.ALOAD, 1));
        mn.instructions.insertBefore(a, new MethodInsnNode(Opcodes.INVOKESTATIC,
            "com/yogpc/mi/TFManager", "hookShowGui", "(Ljava/lang/Object;)V"));
      }
  }

  static boolean findLDC(final MethodNode mn, final String s) {
    AbstractInsnNode ain;
    for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
      if (ain instanceof LdcInsnNode && s.equals(((LdcInsnNode) ain).cst))
        return true;
    return false;
  }

  static boolean startLDC(final MethodNode mn, final String s) {
    AbstractInsnNode ain;
    for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
      if (ain instanceof LdcInsnNode && ((LdcInsnNode) ain).cst instanceof String
          && ((String) ((LdcInsnNode) ain).cst).startsWith(s))
        return true;
    return false;
  }

  private static void gsb(final ClassNode cn) {
    for (final MethodNode mn : cn.methods)
      if ("(Ljava/lang/String;)V".equals(mn.desc)) {
        AbstractInsnNode ain;
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain.getOpcode() == Opcodes.BIPUSH && ((IntInsnNode) ain).operand == 118)
            mn.access =
                mn.access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC;
      }
    for (final FieldNode fn : cn.fields)
      if ("Ljava/lang/String;".equals(fn.desc))
        fn.access = fn.access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC;
  }

  private static void ges(final ClassNode cn) {
    AbstractInsnNode ain;
    for (final MethodNode mn : cn.methods)
      if ("<init>".equals(mn.name)) {
        int phase = 0;
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (phase == 0 && ain.getOpcode() == Opcodes.ALOAD)
            phase = 1;
          else if (phase == 1 && ain.getOpcode() == Opcodes.PUTFIELD) {
            for (final FieldNode fn : cn.fields)
              if (fn.name.equals(((FieldInsnNode) ain).name)) {
                fn.access =
                    fn.access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC;
                break;
              }
            break;
          }
      } else if ("(CI)V".equals(mn.desc))
        for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
          if (ain.getOpcode() == Opcodes.PUTFIELD && ((FieldInsnNode) ain).owner.equals(cn.name)
              && ((FieldInsnNode) ain).desc.equals("I")) {
            for (final FieldNode fn : cn.fields)
              if (fn.name.equals(((FieldInsnNode) ain).name)) {
                fn.access =
                    fn.access & ~(Opcodes.ACC_PRIVATE | Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC;
                break;
              }
            break;
          }
  }

  private static final List<String> hwndmethods = new ArrayList<String>();
  static {
    hwndmethods.add("create(Lorg/lwjgl/opengl/PixelFormat;)V");
    hwndmethods.add("create()V");
    hwndmethods.add("setDisplayMode(Lorg/lwjgl/opengl/DisplayMode;)V");
    hwndmethods.add("setFullscreen(Z)V");
  }

  @Override
  public byte[] transform(final String name, final String transformedName, final byte[] ba) {
    if (ba == null)
      return null;
    if (!done)
      init();
    ClassNode cn = new ClassNode();
    final ClassReader cr = new ClassReader(ba);
    boolean modified = false;
    cr.accept(cn, ClassReader.EXPAND_FRAMES);
    for (final MethodNode mn : cn.methods) {
      AbstractInsnNode ain;
      for (ain = mn.instructions.getFirst(); ain != null; ain = ain.getNext())
        if (ain instanceof MethodInsnNode) {
          final MethodInsnNode min = (MethodInsnNode) ain;
          if (!"org/lwjgl/opengl/Display".equals(min.owner))
            continue;
          if (!hwndmethods.contains(min.name + min.desc))
            continue;
          LogWrapper.info("[MCIME] TFManager.updateWnd call on %s.%s%s", cn.name, mn.name, mn.desc);
          mn.instructions.insert(ain, new MethodInsnNode(Opcodes.INVOKESTATIC,
              "com/yogpc/mi/TFManager", "updateWnd", "()V"));
          modified = true;
        }
    }
    if (cn.name.startsWith("com/yogpc/mi/") && !cn.name.startsWith("com/yogpc/mi/dummy/")
        && !cn.name.startsWith("com/yogpc/mi/asm/")) {
      cn = gtfm(cn);
      modified = true;
    }
    for (final MethodNode mn : cn.methods)
      // TODO Obfuscated detection
      if ("(IIZ)I".equals(mn.desc) && cn.name.indexOf('/') < 0) {
        gtf(cn);
        modified = true;
        // TODO Obfuscated detection
      } else if ("(C)I".equals(mn.desc) && cn.name.indexOf('/') < 0) {
        fr(cn);
        modified = true;
      } else if (findLDC(mn, "########## GL ERROR ##########")) {
        mc(cn);
        modified = true;
        // TODO Obfuscated detection
      } else if ("<init>".equals(mn.name) && findLDC(mn, "pages") && cn.name.indexOf('/') < 0) {
        gsb(cn);
        modified = true;
      } else if (Asm.findLDC(mn, "sign.edit") || Asm.findLDC(mn, "Edit sign message:")) {
        ges(cn);
        modified = true;
      }
    if (!modified)
      return ba;
    final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    cn.accept(cw);
    return cw.toByteArray();
  }
}
