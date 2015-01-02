package com.yogpc.gi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ChatAllowedCharacters;

import org.lwjgl.opengl.GL11;

public class GuiTextFieldManager {
  private final GuiTextField gtf;
  private static boolean jap = false;
  private int japbgn = -1, japend = -1;
  private TranslateEntry[] tea;
  private String raw;

  public GuiTextFieldManager(final GuiTextField gtf) {
    this.gtf = gtf;
  }

  private void reputJap() {
    if (this.japbgn < 0 || this.japend < 0)
      return;
    final String str = this.gtf.getText();
    final StringBuilder sb = new StringBuilder();
    sb.append(str.substring(0, this.japbgn));
    for (final TranslateEntry element : this.tea)
      sb.append(element.hws.length < 1 ? element.ew : element.hws[element.i]);
    final int tmp = sb.length();
    sb.append(str.substring(this.japend));
    this.gtf.setText(sb.toString());
    this.japend = tmp;
  }

  private int getCurrent() {
    int r = this.gtf.getCursorPosition() - this.japbgn;
    for (int i = 0; i < this.tea.length; i++) {
      r -= (this.tea[i].hws.length < 1 ? this.tea[i].ew : this.tea[i].hws[this.tea[i].i]).length();
      if (r <= 0)
        return i;
    }
    return -1;
  }

  private void reset(final boolean full) {
    if (full)
      this.japbgn = -1;
    this.japend = -1;
    this.tea = null;
    this.raw = null;
  }

  public boolean hookTyped(final boolean isEnabled, final char c, final int i) {
    switch (c) {
      case 1:// ctrl+a
        this.gtf.setSelectionPos(0);
        this.gtf.setCursorPositionEnd();
        return true;
      case 10:// ctrl+j
        jap = !jap;
        if (!jap)
          reset(true);
        return true;
      case 3:// ctrl+c
      case 24:// ctrl+x
        GuiScreen.setClipboardString(this.gtf.getSelectedText());
        if (isEnabled && c == 24)
          this.gtf.writeText("");
        return true;
      case 22:// ctrl+v
        if (isEnabled)
          this.gtf.writeText(GuiScreen.getClipboardString());
        return true;
      default:
        switch (i) {
          case 14:// back space
          case 211:// delete
            if (this.japend > -1) {
              final String bef = this.gtf.getText();
              this.gtf.setText(new StringBuilder().append(bef.substring(0, this.japbgn))
                  .append(this.raw).append(bef.substring(this.japend)).toString());
              reset(false);
              return true;
            }
            final int am = i == 14 ? -1 : 1;
            if (isEnabled) {
              if (GuiScreen.isCtrlKeyDown())
                this.gtf.deleteWords(am);
              else
                this.gtf.deleteFromCursor(am);
              if (this.japbgn >= this.gtf.getCursorPosition())
                this.japbgn = -1;
            }
            return true;
          case 199:// home
          case 207:// end
          case 203:// left arrow
          case 205:// right arrow
            final int dir = i == 199 || i == 203 ? -1 : 1;
            final boolean sel = GuiScreen.isShiftKeyDown();
            final int base = sel ? this.gtf.getSelectionEnd() : this.gtf.getCursorPosition();
            final int pos =
                i == 199 || i == 207 ? dir < 0 ? 0 : this.gtf.getText().length() : GuiScreen
                    .isCtrlKeyDown() ? this.gtf.func_146197_a(dir, base, true) : base + dir;
            if (sel)
              this.gtf.setSelectionPos(pos);
            else
              this.gtf.setCursorPosition(pos);
            return true;
          case 200:// up
          case 208:// down
            if (this.tea != null) {
              final int t = getCurrent();
              if (t > -1 && this.tea[t].hws.length > 0) {
                this.tea[t].i =
                    (this.tea[t].i + (i == 200 ? this.tea[t].hws.length - 1 : 1))
                        % this.tea[t].hws.length;
                reputJap();
              }
              return true;
            }
            break;
          case 28:// return
          case 156:// numpadenter
            if (this.japbgn > -1) {
              reset(true);
              return true;
            }
        }
    }
    if (this.japbgn > -1 && c == ' ') {
      final int t = this.tea != null ? getCurrent() : -1;
      if (t > -1) {
        this.tea[t].i++;
        if (this.tea[t].i >= this.tea[t].hws.length)
          this.tea[t].i = 0;
      } else {
        this.japend = this.gtf.getCursorPosition();
        this.raw = this.gtf.getText().substring(this.japbgn, this.japend);
        this.tea = HttpRequest.get(this.raw);
        if (this.tea == null)
          this.japend = -1;
      }
      reputJap();
      return true;
    }
    if (ChatAllowedCharacters.isAllowedCharacter(c)) {
      if (isEnabled) {
        if (this.japend > -1)
          reset(true);
        if (this.japbgn < 0 && c != ' ')
          this.japbgn = this.gtf.getCursorPosition();
        this.gtf.writeText(Character.toString(c));
      }
      return true;
    }
    return false;
  }

  public void hookDraw(final int coff) {
    if (!this.gtf.getVisible() || this.japbgn < 0)
      return;
    final FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
    final String str = this.gtf.getText();
    final int csr = this.gtf.getCursorPosition();
    final int _____japend = this.japend > -1 ? this.japend : csr;
    final int rawbgn = this.japbgn > coff ? fr.getStringWidth(str.substring(coff, this.japbgn)) : 0;
    final int rawend = _____japend > coff ? fr.getStringWidth(str.substring(coff, _____japend)) : 0;
    int selbgn = 0, selend = 0;
    if (this.tea != null) {
      final StringBuilder sb = new StringBuilder(str.length());
      sb.append(str.substring(0, this.japbgn));
      for (final TranslateEntry element : this.tea) {
        final int bgn = sb.length();
        sb.append(element.hws.length < 1 ? element.ew : element.hws[element.i]);
        final int end = sb.length();
        if (csr <= end) {
          selbgn = bgn > coff ? fr.getStringWidth(sb.substring(coff, bgn)) : 0;
          selend = end > coff ? fr.getStringWidth(sb.substring(coff, end)) : 0;
          break;
        }
      }
    }
    final Tessellator tessellator = Tessellator.instance;
    final int bx1 = this.gtf.xPosition;
    final int by1 = this.gtf.yPosition;
    final int by2 = by1 + fr.FONT_HEIGHT;
    final int by3 = by2 + 1;
    GL11.glPushMatrix();
    GL11.glDisable(GL11.GL_TEXTURE_2D);
    GL11.glEnable(GL11.GL_COLOR_LOGIC_OP);
    GL11.glLogicOp(GL11.GL_XOR);
    final int rx1 = bx1 + Math.min(this.gtf.width, rawbgn);
    final int rx2 = bx1 + Math.min(this.gtf.width, rawend);
    if (rx1 < rx2) {
      GL11.glColor4f(255.0F, 0, 0, 255.0F);
      tessellator.startDrawingQuads();
      tessellator.addVertex(rx1, by3, 0.0D);
      tessellator.addVertex(rx2, by3, 0.0D);
      tessellator.addVertex(rx2, by2, 0.0D);
      tessellator.addVertex(rx1, by2, 0.0D);
      tessellator.draw();
    }
    final int sx1 = bx1 + Math.min(this.gtf.width, selbgn);
    final int sx2 = bx1 + Math.min(this.gtf.width, selend);
    if (sx1 < sx2) {
      GL11.glColor4f(255.0F, 255.0F, 255.0F, 255.0F);
      tessellator.startDrawingQuads();
      tessellator.addVertex(sx1, by2, 0.0D);
      tessellator.addVertex(sx2, by2, 0.0D);
      tessellator.addVertex(sx2, by1, 0.0D);
      tessellator.addVertex(sx1, by1, 0.0D);
      tessellator.draw();
    }
    GL11.glDisable(GL11.GL_COLOR_LOGIC_OP);
    GL11.glEnable(GL11.GL_TEXTURE_2D);
    GL11.glPopMatrix();
  }
}
