package com.yogpc.gi;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import org.lwjgl.opengl.GL11;

import com.yogpc.gi.dummy.FontRenderer;
import com.yogpc.gi.dummy.GuiEditSign;
import com.yogpc.gi.dummy.GuiScreen;
import com.yogpc.gi.dummy.GuiScreenBook;
import com.yogpc.gi.dummy.Minecraft;
import com.yogpc.gi.w32.JNIHandler;

public class TFManager {
  private static WeakReference<Handler> cur = new WeakReference<Handler>(null);
  private static Map<GTFHandler, Long> map = new WeakHashMap<GTFHandler, Long>();
  private static boolean enabled = false;

  public static final void hookShowGui(final Object o) {
    map.clear();
    if (o instanceof GuiEditSign || o instanceof GuiScreenBook) {
      // TODO Handler for GuiEditSign and GuiScreenBook
      cur = new WeakReference<Handler>(null);
      JNIHandler.linkIME();
    } else {
      cur = new WeakReference<Handler>(null);
      JNIHandler.unlinkIME();
    }
  }

  public static final void hookDrawGui() {
    final Handler h = cur.get();
    if (!enabled || h == null)
      return;
    final Minecraft mc = Minecraft.getMinecraft();
    final FontRenderer fr = mc.fontRenderer;
    final GuiScreen cs = mc.currentScreen;
    fr.drawString("あ", cs.width - fr.getCharWidth('あ') - 2, cs.height - (9 + 2), 0x00FFFF);
    if (h.a == null)
      return;
    GL11.glPushMatrix();
    GL11.glEnable(GL11.GL_ALPHA_TEST);
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    final int bgn = h.clist.length == h.a.count ? 0 : h.cpos - h.a.count / 2;
    GL11.glColor4f(0, 0, 0, 0.5F);
    GL11.glBegin(GL11.GL_QUADS);
    GL11.glVertex3f(h.a.xpos, h.a.ypos, 0);
    GL11.glVertex3f(h.a.xpos, h.a.ypos + h.a.count * (9/* FontRenderer.FONT_HEIGHT */+ 2), 0);
    GL11.glVertex3f(h.a.xmax, h.a.ypos + h.a.count * (9/* FontRenderer.FONT_HEIGHT */+ 2), 0);
    GL11.glVertex3f(h.a.xmax, h.a.ypos, 0);
    GL11.glEnd();
    GL11.glDisable(GL11.GL_BLEND);
    GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
    GL11.glLogicOp(GL11.GL_XOR);
    GL11.glColor4f(0, 0, 1, 1);
    GL11.glBegin(GL11.GL_QUADS);
    GL11.glVertex3f(h.a.xpos, h.a.ypos + (h.cpos - bgn) * (9/* FontRenderer.FONT_HEIGHT */+ 2), 0);
    GL11.glVertex3f(h.a.xpos, h.a.ypos + (h.cpos - bgn + 1) * (9/* FontRenderer.FONT_HEIGHT */+ 2),
        0);
    GL11.glVertex3f(h.a.xmax, h.a.ypos + (h.cpos - bgn + 1) * (9/* FontRenderer.FONT_HEIGHT */+ 2),
        0);
    GL11.glVertex3f(h.a.xmax, h.a.ypos + (h.cpos - bgn) * (9/* FontRenderer.FONT_HEIGHT */+ 2), 0);
    GL11.glEnd();
    GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    int cypos = h.a.ypos;
    for (int i = 0; i < h.a.count; i++) {
      h.a.fr.drawString(h.clist[i + bgn], h.a.xpos, cypos + 1, 0xFFFFFF);
      cypos += 9/* FontRenderer.FONT_HEIGHT */+ 2;
    }
    GL11.glPopMatrix();
    h.a = null;
  }

  public static final void hookFocuse(final GTFHandler o, final boolean fc, final boolean en) {
    map.put(o, Long.valueOf(fc && en ? System.nanoTime() : -1));
    GTFHandler r = null;
    long l = -1;
    for (final Map.Entry<GTFHandler, Long> e : map.entrySet())
      if (l < e.getValue().longValue()) {
        r = e.getKey();
        l = e.getValue().longValue();
      }
    if (r != null) {
      cur = new WeakReference<Handler>(r);
      JNIHandler.linkIME();
    } else {
      cur = new WeakReference<Handler>(null);
      JNIHandler.unlinkIME();
    }
  }

  public static final void updateWnd() {
    JNIHandler.updateHWnd();
    if (cur.get() != null)
      JNIHandler.linkIME();
    else
      JNIHandler.unlinkIME();
  }

  public static final boolean shouldKill() {
    return cur.get() == null;
  }

  public static final void pushComposition(final char[] c, final byte[] b, final int p) {
    final Handler i = cur.get();
    if (i != null)
      i.pushComposition(c, b, p);
  }

  public static final void pushResult(final String sn) {
    final Handler i = cur.get();
    if (i != null)
      i.pushResult(sn);
  }

  public static final void pushCandidate(final String[] s, final int c) {
    final Handler i = cur.get();
    if (i != null)
      i.pushCandidate(s, c);
  }

  public static final void pushStatus(final boolean b) {
    enabled = b;
  }
}
