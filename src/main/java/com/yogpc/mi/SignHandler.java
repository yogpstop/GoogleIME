package com.yogpc.mi;

import com.yogpc.mi.dummy.ChatComponentText;
import com.yogpc.mi.dummy.GuiEditSign;
import com.yogpc.mi.dummy.IChatComponent;

public class SignHandler extends Handler {
  final GuiEditSign te;

  public SignHandler(final GuiEditSign ges) {
    this.te = ges;
  }

  @Override
  protected String getText() {
    final Object o = this.te.tile.signText[this.te.line];
    if (o instanceof String)
      return (String) o;
    else if (o instanceof IChatComponent)
      return ((IChatComponent) o).getUnformattedText();
    return "";
  }

  @Override
  protected int getCursorPosition() {
    return getText().length();
  }

  @Override
  protected void setText(final String s) {
    final Object[] oa = this.te.tile.signText;
    if (oa instanceof String[])
      oa[this.te.line] = s;
    else if (oa instanceof IChatComponent[])
      oa[this.te.line] = new ChatComponentText(s);
  }

  @Override
  protected void setCursorPosition(final int i) {
    // unsupported operation
  }
}
