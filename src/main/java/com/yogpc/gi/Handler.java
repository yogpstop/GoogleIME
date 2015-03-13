package com.yogpc.gi;

public abstract class Handler {
  protected int from = -1;
  protected byte[] attrs = null;
  protected boolean enabled = false;


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
    this.from += sn.length();
  }

  protected String[] clist;
  protected int cpos;

  public final void pushCandidate(final String[] s, final int c) {
    this.clist = s;
    this.cpos = c;
  }

  public final void pushStatus(final boolean b) {
    this.enabled = b;
  }
}
