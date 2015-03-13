package com.yogpc.gi;

import org.lwjgl.opengl.GL11;

import com.yogpc.gi.dummy.FontRenderer;
import com.yogpc.gi.dummy.GuiTextField;
import com.yogpc.gi.dummy.Tessellator;
import com.yogpc.gi.dummy.WorldRenderer;

public class GTFHandler extends Handler {
  private final GuiTextField gtf;

  public GTFHandler(final GuiTextField gtf) {
    this.gtf = gtf;
  }

  public void hookDraw(final int coff, final Object o, final int xpos, final int ypos,
      final int width) {
    // TODO render status and candidate
    if (this.attrs == null)
      return;
    final FontRenderer fr = (FontRenderer) o;
    final Tessellator tessellator = Tessellator.getInstance();
    final WorldRenderer wr = tessellator.getWorldRenderer();
    final int by1 = ypos;
    final int by2 = by1 + 9;// FontRenderer.FONT_HEIGHT
    final int by3 = by2 + 1;
    final String str = this.gtf.getText();
    GL11.glPushMatrix();
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
    GL11.glLogicOp(GL11.GL_XOR);
    int pw = xpos;
    boolean bold = false;
    for (int i = 0; i < str.length(); i++) {
      if (pw > xpos + width || i - this.from >= this.attrs.length)
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
      pw += w;
      w = pw - w;
      if (i < this.from)
        continue;
      final int attr = this.attrs[i - this.from];
      GL11.glColor4f(255.0F, 0, 0, 255.0F);
      wr.startDrawingQuads();
      wr.addVertex(w, by3, 0.0D);
      wr.addVertex(pw, by3, 0.0D);
      wr.addVertex(pw, by2, 0.0D);
      wr.addVertex(w, by2, 0.0D);
      tessellator.draw();
      if (attr == 1 || attr == 3) {
        GL11.glColor4f(255.0F, 255.0F, 255.0F, 255.0F);
        wr.startDrawingQuads();
        wr.addVertex(w, by2, 0.0D);
        wr.addVertex(pw, by2, 0.0D);
        wr.addVertex(pw, by1, 0.0D);
        wr.addVertex(w, by1, 0.0D);
        tessellator.draw();
      }
      if (attr == 1 || attr == 2) {
        GL11.glColor4f(0, 0, 255.0F, 255.0F);
        wr.startDrawingQuads();
        wr.addVertex(w, by2, 0.0D);
        wr.addVertex(pw, by2, 0.0D);
        wr.addVertex(pw, by1, 0.0D);
        wr.addVertex(w, by1, 0.0D);
        tessellator.draw();
      }
    }
    GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    GL11.glPopMatrix();
    return;
  }

  @Override
  protected String getText() {
    return this.gtf.getText();
  }

  @Override
  protected int getCursorPosition() {
    return this.gtf.getCursorPosition();
  }

  @Override
  protected void setText(final String s) {
    this.gtf.setText(s);
  }

  @Override
  protected void setCursorPosition(final int i) {
    this.gtf.setCursorPosition(i);
  }
}
