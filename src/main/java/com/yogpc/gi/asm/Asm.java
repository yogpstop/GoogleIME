package com.yogpc.gi.asm;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class Asm implements IClassTransformer {
  private static final String focuse[] = new String[2];

  private static final void key(final MethodNode mn, final String cn) {
    AbstractInsnNode a;
    final Iterator<AbstractInsnNode> i = mn.instructions.iterator();
    while (i.hasNext()) {
      a = i.next();
      if (a instanceof FieldInsnNode && "Z".equals(((FieldInsnNode) a).desc)
          && cn.equals(((FieldInsnNode) a).owner)) {
        if (((FieldInsnNode) a).name.equals(focuse[0])
            || ((FieldInsnNode) a).name.equals(focuse[1]))
          continue;
        if (focuse[0] == null)
          focuse[0] = ((FieldInsnNode) a).name;
        else if (focuse[1] == null)
          focuse[1] = ((FieldInsnNode) a).name;
      }
    }
  }

  private static final void init(final MethodNode mn, final String cn) {
    AbstractInsnNode p = mn.instructions.getLast();
    while (p.getOpcode() != Opcodes.RETURN)
      p = p.getPrevious();
    p = p.getPrevious();
    mn.instructions.insert(p, new FieldInsnNode(Opcodes.PUTFIELD, cn, "manager",
        "Lcom/yogpc/gi/GTFHandler;"));
    mn.instructions.insert(p, new MethodInsnNode(Opcodes.INVOKESPECIAL, "com/yogpc/gi/GTFHandler",
        "<init>", "(L" + cn + ";)V", false));
    mn.instructions.insert(p, new VarInsnNode(Opcodes.ALOAD, 0));
    mn.instructions.insert(p, new InsnNode(Opcodes.DUP));
    mn.instructions.insert(p, new TypeInsnNode(Opcodes.NEW, "com/yogpc/gi/GTFHandler"));
    mn.instructions.insert(p, new VarInsnNode(Opcodes.ALOAD, 0));
  }

  private static final void draw(final MethodNode mn, final String cn, final String fn,
      final String frt, final String frf, final String xpos, final String ypos, final String width) {
    AbstractInsnNode p = mn.instructions.getFirst();
    while (p != null) {
      if (p.getOpcode() == Opcodes.RETURN) {
        mn.instructions.insertBefore(p, new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.insertBefore(p, new FieldInsnNode(Opcodes.GETFIELD, cn, "manager",
            "Lcom/yogpc/gi/GTFHandler;"));
        mn.instructions.insertBefore(p, new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.insertBefore(p, new FieldInsnNode(Opcodes.GETFIELD, cn, fn, "I"));
        mn.instructions.insertBefore(p, new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.insertBefore(p, new FieldInsnNode(Opcodes.GETFIELD, cn, frf, frt));
        mn.instructions.insertBefore(p, new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.insertBefore(p, new FieldInsnNode(Opcodes.GETFIELD, cn, xpos, "I"));
        mn.instructions.insertBefore(p, new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.insertBefore(p, new FieldInsnNode(Opcodes.GETFIELD, cn, ypos, "I"));
        mn.instructions.insertBefore(p, new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.insertBefore(p, new FieldInsnNode(Opcodes.GETFIELD, cn, width, "I"));
        mn.instructions.insertBefore(p, new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
            "com/yogpc/gi/GTFHandler", "hookDraw", "(ILjava/lang/Object;III)V", false));
      }
      p = p.getNext();
    }
  }

  private static final void count(final MethodNode mn, final String cn,
      final Map<String, Integer> map) {
    AbstractInsnNode p = mn.instructions.getFirst();
    while (p != null) {
      if ((p.getOpcode() == Opcodes.GETFIELD || p.getOpcode() == Opcodes.PUTFIELD)
          && cn.equals(((FieldInsnNode) p).owner) && "I".equals(((FieldInsnNode) p).desc)) {
        final Integer i = map.get(((FieldInsnNode) p).name);
        final int j = i == null ? 1 : i.intValue() + 1;
        map.put(((FieldInsnNode) p).name, Integer.valueOf(j));
      }
      p = p.getNext();
    }
  }

  private static final void gtf(final ClassNode cn) {
    cn.fields.add(new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "manager",
        "Lcom/yogpc/gi/GTFHandler;", null, null));
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
    String frf = null, frt = null, xpos = null, ypos = null, width = null;
    for (final MethodNode mn : cn.methods)
      if (mn.name.equals("<init>")) {
        final int shift = mn.desc.charAt(1) == 'L' ? 0 : 1;
        int phase = -1;
        AbstractInsnNode ain = mn.instructions.getFirst();
        while (ain != null) {
          if (phase < 0 && ain.getOpcode() == Opcodes.ALOAD && ((VarInsnNode) ain).var == 0)
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
              case 9999:
                frf = ((FieldInsnNode) ain).name;
                frt = ((FieldInsnNode) ain).desc;
                break;
            }
            phase = -1;
          } else
            phase = -1;
          ain = ain.getNext();
        }
      }
    for (final MethodNode mnode : cn.methods)
      if ("(CI)Z".equals(mnode.desc))
        key(mnode, cn.name);
      else if ("<init>".equals(mnode.name))
        init(mnode, cn.name);
      else if ("()V".equals(mnode.desc) && mnode.instructions.size() > 150)
        draw(mnode, cn.name, maxK, frt, frf, xpos, ypos, width);
    for (final MethodNode mn : cn.methods) {
      AbstractInsnNode ain = mn.instructions.getFirst();
      while (ain != null) {
        fs: if (ain.getOpcode() == Opcodes.PUTFIELD) {
          final FieldInsnNode min = (FieldInsnNode) ain;
          if (!cn.name.equals(min.owner) || !"Z".equals(min.desc))
            break fs;
          if (!min.name.equals(focuse[0]) && !min.name.equals(focuse[1]))
            break fs;
          mn.instructions.insert(ain, new MethodInsnNode(Opcodes.INVOKESTATIC,
              "com/yogpc/gi/TFManager", "hookFocuse", "(Lcom/yogpc/gi/GTFHandler;ZZ)V", false));
          mn.instructions.insert(ain, new FieldInsnNode(Opcodes.GETFIELD, cn.name, focuse[1], "Z"));
          mn.instructions.insert(ain, new VarInsnNode(Opcodes.ALOAD, 0));
          mn.instructions.insert(ain, new FieldInsnNode(Opcodes.GETFIELD, cn.name, focuse[0], "Z"));
          mn.instructions.insert(ain, new VarInsnNode(Opcodes.ALOAD, 0));
          mn.instructions.insert(ain, new FieldInsnNode(Opcodes.GETFIELD, cn.name, "manager",
              "Lcom/yogpc/gi/GTFHandler;"));
          mn.instructions.insert(ain, new VarInsnNode(Opcodes.ALOAD, 0));
        }
        ain = ain.getNext();
      }
    }
  }

  private static boolean done = false;

  private static final ClassNode gtfm(final ClassNode cn) {
    try {
      if (!done) {
        final URL[] urls = Launch.classLoader.getURLs();
        for (final URL url : urls) {
          boolean isTarget = false;
          ZipEntry ze;
          final InputStream is = url.openStream();
          final ZipInputStream in = new ZipInputStream(is);
          while ((ze = in.getNextEntry()) != null) {
            if (ze.getName().startsWith("META-INF/MOJANG")) {
              isTarget = true;
              break;
            }
            in.closeEntry();
          }
          in.close();
          is.close();
          if (isTarget)
            Analyzer.anis(url);
        }
        done = true;
      }
      final ClassNode out = new ClassNode();
      cn.accept(AsmFixer.InitAdapter(out, Mapping.I));
      return out;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void mc(final ClassNode cn) {
    final List<String> map = new ArrayList<String>();
    for (final MethodNode mn : cn.methods)
      if ("(II)V".equals(mn.desc)) {
        AbstractInsnNode ain = mn.instructions.getFirst();
        while (ain != null) {
          if (ain instanceof FieldInsnNode && ((FieldInsnNode) ain).owner.equals(cn.name)
              && ((FieldInsnNode) ain).desc.startsWith("L"))
            map.add("(" + ((FieldInsnNode) ain).desc + ")V");
          ain = ain.getNext();
        }
      }
    for (final MethodNode mn : cn.methods)
      if (map.contains(mn.desc)) {
        final AbstractInsnNode a = mn.instructions.getFirst();
        mn.instructions.insertBefore(a, new VarInsnNode(Opcodes.ALOAD, 1));
        mn.instructions.insertBefore(a, new MethodInsnNode(Opcodes.INVOKESTATIC,
            "com/yogpc/gi/TFManager", "hookShowGui", "(Ljava/lang/Object;)V", false));
      }
  }

  private static boolean isMinecraft(final MethodNode mn) {
    AbstractInsnNode ain = mn.instructions.getFirst();
    while (ain != null) {
      if (ain instanceof LdcInsnNode
          && "########## GL ERROR ##########".equals(((LdcInsnNode) ain).cst))
        return true;
      ain = ain.getNext();
    }
    return false;
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
    ClassNode cn = new ClassNode();
    final ClassReader cr = new ClassReader(ba);
    boolean modified = false;
    cr.accept(cn, ClassReader.EXPAND_FRAMES);
    for (final MethodNode mn : cn.methods) {
      AbstractInsnNode ain = mn.instructions.getFirst();
      while (ain != null) {
        fs: if (ain instanceof MethodInsnNode) {
          final MethodInsnNode min = (MethodInsnNode) ain;
          if (!"org/lwjgl/opengl/Display".equals(min.owner))
            break fs;
          if (!hwndmethods.contains(min.name + min.desc))
            break fs;
          mn.instructions.insert(ain, new MethodInsnNode(Opcodes.INVOKESTATIC,
              "com/yogpc/gi/TFManager", "updateWnd", "()V", false));
          modified = true;
        }
        ain = ain.getNext();
      }
    }
    if ("com.yogpc.gi.GTFHandler".equals(name) || "com.yogpc.gi.TFManager".equals(name)) {
      cn = gtfm(cn);
      modified = true;
    }
    if (name.length() < 4)// TODO Obfuscated detection
      for (final MethodNode mn : cn.methods)
        if ("(IIZ)I".equals(mn.desc)) {
          gtf(cn);
          modified = true;
        } else if (isMinecraft(mn)) {
          mc(cn);
          modified = true;
        }
    if (!modified)
      return ba;
    final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    cn.accept(cw);
    return cw.toByteArray();
  }
}
