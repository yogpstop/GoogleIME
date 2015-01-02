package com.yogpc.gi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.relauncher.FMLRelaunchLog;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

public class Asm extends DummyModContainer implements IFMLLoadingPlugin, IClassTransformer {
  private static boolean initialized = false;
  private static final ModMetadata md = new ModMetadata();
  private static String GTB, GC;
  static {
    md.modId = "googleime";
    md.name = "GoogleIME";
  }

  public Asm() {
    super(md);
  }

  @Override
  public String[] getASMTransformerClass() {
    return new String[] {this.getClass().getName()};
  }

  @Override
  public String getModContainerClass() {
    return this.getClass().getName();
  }

  @Override
  public String getSetupClass() {
    return null;
  }

  @Override
  public void injectData(final Map<String, Object> data) {}

  @Override
  public String getAccessTransformerClass() {
    return null;
  }

  private static final void init() {
    GTB = FMLDeobfuscatingRemapper.INSTANCE.unmap("net/minecraft/client/gui/GuiTextField");
    GC = FMLDeobfuscatingRemapper.INSTANCE.unmap("net/minecraft/client/gui/GuiChat");
    initialized = true;
  }

  private static final void key(final MethodNode mn, final String cn) {
    String f1 = null, f2 = null;
    AbstractInsnNode a;
    final Iterator<AbstractInsnNode> i = mn.instructions.iterator();
    while (i.hasNext()) {
      a = i.next();
      if (a instanceof FieldInsnNode && "Z".equals(((FieldInsnNode) a).desc)
          && cn.equals(((FieldInsnNode) a).owner)) {
        if (((FieldInsnNode) a).name.equals(f1) || ((FieldInsnNode) a).name.equals(f2))
          continue;
        if (f1 == null)
          f1 = ((FieldInsnNode) a).name;
        else if (f2 == null)
          f2 = ((FieldInsnNode) a).name;
        else
          FMLRelaunchLog.warning("Overflow own boolean field %s\n", ((FieldInsnNode) a).name);
      }
    }
    mn.instructions.clear();
    final LabelNode l = new LabelNode();
    mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    mn.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, cn, f1, "Z"));
    mn.instructions.add(new JumpInsnNode(Opcodes.IFNE, l));
    mn.instructions.add(new InsnNode(Opcodes.ICONST_0));
    mn.instructions.add(new InsnNode(Opcodes.IRETURN));
    mn.instructions.add(l);
    mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    mn.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, cn, "manager",
        "Lcom/yogpc/gi/GuiTextFieldManager;"));
    mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
    mn.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, cn, f2, "Z"));
    mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1));
    mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 2));
    mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
        "com/yogpc/gi/GuiTextFieldManager", "hookTyped", "(ZCI)Z", false));
    mn.instructions.add(new InsnNode(Opcodes.IRETURN));
  }

  private static final void init(final MethodNode mn, final String cn) {
    AbstractInsnNode p = mn.instructions.getLast();
    while (p.getOpcode() != Opcodes.RETURN)
      p = p.getPrevious();
    p = p.getPrevious();
    mn.instructions.insert(p, new FieldInsnNode(Opcodes.PUTFIELD, cn, "manager",
        "Lcom/yogpc/gi/GuiTextFieldManager;"));
    mn.instructions.insert(p, new MethodInsnNode(Opcodes.INVOKESPECIAL,
        "com/yogpc/gi/GuiTextFieldManager", "<init>", "(L" + cn + ";)V", false));
    mn.instructions.insert(p, new VarInsnNode(Opcodes.ALOAD, 0));
    mn.instructions.insert(p, new InsnNode(Opcodes.DUP));
    mn.instructions.insert(p, new TypeInsnNode(Opcodes.NEW, "com/yogpc/gi/GuiTextFieldManager"));
    mn.instructions.insert(p, new VarInsnNode(Opcodes.ALOAD, 0));
  }

  private static final void draw(final MethodNode mn, final String cn, final String fn) {
    AbstractInsnNode p = mn.instructions.getFirst();
    while (p != null) {
      if (p.getOpcode() == Opcodes.RETURN) {
        mn.instructions.insertBefore(p, new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.insertBefore(p, new FieldInsnNode(Opcodes.GETFIELD, cn, "manager",
            "Lcom/yogpc/gi/GuiTextFieldManager;"));
        mn.instructions.insertBefore(p, new VarInsnNode(Opcodes.ALOAD, 0));
        mn.instructions.insertBefore(p, new FieldInsnNode(Opcodes.GETFIELD, cn, fn, "I"));
        mn.instructions.insertBefore(p, new MethodInsnNode(Opcodes.INVOKEVIRTUAL,
            "com/yogpc/gi/GuiTextFieldManager", "hookDraw", "(I)V", false));
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

  private static final byte[] gtb(final byte[] ba) {
    final ClassNode cnode = new ClassNode();
    final ClassReader reader = new ClassReader(ba);
    reader.accept(cnode, ClassReader.EXPAND_FRAMES);
    cnode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "manager",
        "Lcom/yogpc/gi/GuiTextFieldManager;", null, null));
    final Map<String, Integer> map = new HashMap<String, Integer>();
    for (final MethodNode mnode : cnode.methods)
      if ("(I)V".equals(mnode.desc))
        count(mnode, cnode.name, map);
    int maxV = 0;
    String maxK = null;
    for (final Map.Entry<String, Integer> e : map.entrySet())
      if (e.getValue().intValue() > maxV) {
        maxV = e.getValue().intValue();
        maxK = e.getKey();
      }
    for (final MethodNode mnode : cnode.methods)
      if ("(CI)Z".equals(mnode.desc))
        key(mnode, cnode.name);
      else if ("<init>".equals(mnode.name))
        init(mnode, cnode.name);
      else if ("()V".equals(mnode.desc) && mnode.instructions.size() > 150)
        draw(mnode, cnode.name, maxK);
    final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    cnode.accept(cw);
    return cw.toByteArray();
  }

  private static final void chat(final MethodNode mn, final String cn, final String fn,
      final String fd) {
    String fmn = null, fmo = null;
    AbstractInsnNode a;
    final Iterator<AbstractInsnNode> i = mn.instructions.iterator();
    while (i.hasNext()) {
      a = i.next();
      if (a instanceof MethodInsnNode && "(CI)Z".equals(((MethodInsnNode) a).desc)) {
        fmo = ((MethodInsnNode) a).owner;
        fmn = ((MethodInsnNode) a).name;
        break;
      }
    }
    a = mn.instructions.getFirst();
    mn.instructions.insertBefore(a, new VarInsnNode(Opcodes.ALOAD, 0));
    mn.instructions.insertBefore(a, new FieldInsnNode(Opcodes.GETFIELD, cn, fn, fd));
    mn.instructions.insertBefore(a, new VarInsnNode(Opcodes.ILOAD, 1));
    mn.instructions.insertBefore(a, new VarInsnNode(Opcodes.ILOAD, 2));
    mn.instructions.insertBefore(a, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, fmo, fmn, "(CI)Z",
        false));
    final LabelNode l = new LabelNode();
    mn.instructions.insertBefore(a, new JumpInsnNode(Opcodes.IFEQ, l));
    mn.instructions.insertBefore(a, new InsnNode(Opcodes.RETURN));
    mn.instructions.insertBefore(a, l);
  }

  private static final byte[] gc(final byte[] ba) {
    final ClassNode cnode = new ClassNode();
    final ClassReader reader = new ClassReader(ba);
    reader.accept(cnode, ClassReader.EXPAND_FRAMES);
    final String cd = "L" + GTB + ";";
    String fn = null;
    for (final FieldNode fnode : cnode.fields)
      if (cd.equals(fnode.desc))
        fn = fnode.name;
    for (final MethodNode mnode : cnode.methods)
      if ("(CI)V".equals(mnode.desc))
        chat(mnode, cnode.name, fn, cd);
    final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    cnode.accept(cw);
    return cw.toByteArray();
  }

  @Override
  public byte[] transform(final String name, final String transformedName, final byte[] ba) {
    if (!initialized)
      init();
    final String un = name.replace('.', '/');
    if (GTB.equals(un))
      return gtb(ba);
    if (GC.equals(un))
      return gc(ba);
    return ba;
  }
}
