package com.yogpc.gi;

import com.yogpc.gi.dummy.FontRenderer;
import com.yogpc.gi.dummy.GuiTextField;

public class GTFHandler extends Handler {
  private final GuiTextField gtf;

  public GTFHandler(final GuiTextField gtf) {
    this.gtf = gtf;
  }

  public void hookDraw(final int coff, final Object o, final int xpos, final int ypos,
      final int width, final int height, final boolean bg) {
    if (this.attrs == null)
      return;
    final FontRenderer fr = (FontRenderer) o;
    final int aypos = ypos + (bg ? (height - 8) / 2 : 0);
    final String str = this.gtf.getText();
    boolean bold = false;
    int xposc = xpos + (bg ? 4 : 0);
    final int xmax = xpos + width;
    renderOverride(str, fr, xposc, aypos, xmax, coff);
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
      if (attr == 1 || attr == 3) {
        xposc = w;
        break;
      }
    }
    renderCandidate(fr, xposc, aypos, aypos + 9/* FontRenderer.FONT_HEIGHT */);
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
