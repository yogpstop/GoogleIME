package com.yogpc.gi;

import org.lwjgl.opengl.GL11;

import com.yogpc.gi.dummy.FontRenderer;
import com.yogpc.gi.dummy.GuiScreen;
import com.yogpc.gi.dummy.Minecraft;

public abstract class Handler {
  protected int from = -1;
  protected byte[] attrs = null;


  protected abstract String getText();

  protected abstract int getCursorPosition();

  protected abstract void setText(String s);

  protected abstract void setCursorPosition(int i);

  // TODO wishful thinking.
  public final void pushComposition(final char[] c, final byte[] b, final int p) {
    String s = getText();
    final String sn = c != null ? new String(c) : "";
    int cp = getCursorPosition();
    final int p1 = Math.max(0, Math.min(s.length(), this.from > -1 ? this.from : cp));
    final int p2 =
        Math.max(0, Math.min(s.length(), this.from > -1 ? this.from + this.attrs.length : cp));
    s = s.substring(0, p1) + sn + s.substring(p2);
    setText(s);
    if (c != null)
      cp = p1 + p;
    else if (cp > p1)
      if (cp < p2)
        cp = p1;
      else
        cp = cp - (p2 - p1);
    setCursorPosition(cp);
    if (c != null)
      this.from = p1;
    else
      this.from = -1;
    this.attrs = b;
  }

  public final void pushResult(final String sn) {
    String s = getText();
    final int p =
        Math.max(0, Math.min(s.length(), this.from > -1 ? this.from : getCursorPosition()));
    s = s.substring(0, p) + sn + s.substring(p);
    setText(s);
    setCursorPosition(p + sn.length());
    if (this.from > -1)
      this.from += sn.length();
  }

  protected String[] clist;
  protected int cpos;

  public final void pushCandidate(final String[] s, final int c) {
    this.clist = s;
    this.cpos = c;
  }

  protected static final int getStringWidth(final FontRenderer fr, final String str) {
    int r = 0;
    boolean bold = false;
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      int w = fr.getCharWidth(c);
      if (w < 0) {
        w = 0;
        if (i < str.length() - 1) {
          c = str.charAt(++i);
          if (c == 'l' || c == 'L')
            bold = true;
          else if (c == 'r' || c == 'R')
            bold = false;
        }
      }
      if (bold && w > 0)
        ++w;
      r += w;
    }
    return r;
  }

  private final void iRenderCandidate(final FontRenderer fr, final int xpos, final int ypos,
      final int count, final int xmax) {
    GL11.glPushMatrix();
    GL11.glEnable(GL11.GL_ALPHA_TEST);
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glEnable(GL11.GL_BLEND);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    final int bgn = this.clist.length == count ? 0 : this.cpos - count / 2;
    GL11.glColor4f(0, 0, 0, 127.0F);
    GL11.glBegin(GL11.GL_QUADS);
    GL11.glVertex3f(xpos, ypos, 0);
    GL11.glVertex3f(xpos, ypos + count * (9/* FontRenderer.FONT_HEIGHT */+ 2), 0);
    GL11.glVertex3f(xmax, ypos + count * (9/* FontRenderer.FONT_HEIGHT */+ 2), 0);
    GL11.glVertex3f(xmax, ypos, 0);
    GL11.glEnd();
    GL11.glDisable(GL11.GL_BLEND);
    GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
    GL11.glLogicOp(GL11.GL_XOR);
    GL11.glColor4f(0, 0, 255.0F, 255.0F);
    GL11.glBegin(GL11.GL_QUADS);
    GL11.glVertex3f(xpos, ypos + (this.cpos - bgn) * (9/* FontRenderer.FONT_HEIGHT */+ 2), 0);
    GL11.glVertex3f(xpos, ypos + (this.cpos - bgn + 1) * (9/* FontRenderer.FONT_HEIGHT */+ 2), 0);
    GL11.glVertex3f(xmax, ypos + (this.cpos - bgn + 1) * (9/* FontRenderer.FONT_HEIGHT */+ 2), 0);
    GL11.glVertex3f(xmax, ypos + (this.cpos - bgn) * (9/* FontRenderer.FONT_HEIGHT */+ 2), 0);
    GL11.glEnd();
    GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    int cypos = ypos;
    for (int i = 0; i < count; i++) {
      fr.drawString(this.clist[i + bgn], xpos, cypos + 1, 0xFFFFFF);
      cypos += 9/* FontRenderer.FONT_HEIGHT */+ 2;
    }
    GL11.glPopMatrix();
  }

  protected final void renderCandidate(final FontRenderer fr, final int xpos, final int ypos1,
      final int ypos2) {
    final GuiScreen gs = Minecraft.getMinecraft().currentScreen;
    if (gs == null || this.clist == null)
      return;
    int xlen = 0;
    for (final String str : this.clist)
      xlen = Math.max(xlen, getStringWidth(fr, str));
    xlen += 2;
    final int aylen2 = gs.height - ypos2;
    final boolean top = ypos1 > aylen2;
    // FontRenderer.FONT_HEIGHT = 9
    final int count = Math.min(this.clist.length, (top ? ypos1 : aylen2) / (9 + 2));
    iRenderCandidate(fr, gs.width < xpos + xlen ? gs.width - xlen : xpos, top ? ypos1 - count
        * (9/* FontRenderer.FONT_HEIGHT */+ 2) : ypos2, count, xpos + xlen);
  }

  protected final void renderOverride(final String str, final FontRenderer fr, final int xpos,
      final int ypos1, final int xmax, final int coff) {
    final int ypos2 = ypos1 + 9;// FontRenderer.FONT_HEIGHT
    final int ypos3 = ypos2 + 1;
    int xposc = xpos;
    boolean bold = false;
    GL11.glPushMatrix();
    GL11.glEnable(GL11.GL_ALPHA_TEST);
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
    GL11.glLogicOp(GL11.GL_XOR);
    for (int i = 0; i < str.length(); i++) {
      if (xposc > xmax || i - this.from >= this.attrs.length)
        break;
      char c = str.charAt(i);
      int w = fr.getCharWidth(c);
      if (w < 0) {
        w = 0;
        if (i < str.length() - 1) {
          c = str.charAt(++i);
          if (c == 'l' || c == 'L')
            bold = true;
          else if (c == 'r' || c == 'R')
            bold = false;
        }
      }
      if (bold && w > 0)
        ++w;
      if (i < coff)
        continue;
      xposc += w;
      w = xposc - w;
      if (i < this.from)
        continue;
      final int attr = this.attrs[i - this.from];
      GL11.glColor4f(255.0F, 0, 0, 255.0F);
      GL11.glBegin(GL11.GL_QUADS);
      GL11.glVertex3f(w, ypos3, 0);
      GL11.glVertex3f(xposc, ypos3, 0);
      GL11.glVertex3f(xposc, ypos2, 0);
      GL11.glVertex3f(w, ypos2, 0);
      GL11.glEnd();
      if (attr == 1 || attr == 3) {
        GL11.glColor4f(255.0F, 255.0F, 255.0F, 255.0F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(w, ypos2, 0);
        GL11.glVertex3f(xposc, ypos2, 0);
        GL11.glVertex3f(xposc, ypos1, 0);
        GL11.glVertex3f(w, ypos1, 0);
        GL11.glEnd();
      }
      if (attr == 1 || attr == 2) {
        GL11.glColor4f(0, 0, 255.0F, 255.0F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(w, ypos2, 0);
        GL11.glVertex3f(xposc, ypos2, 0);
        GL11.glVertex3f(xposc, ypos1, 0);
        GL11.glVertex3f(w, ypos1, 0);
        GL11.glEnd();
      }
    }
    GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    GL11.glPopMatrix();
  }
}
