package com.yogpc.mi.dummy;

@SuppressWarnings({"static-method", "unused"})
public class FontRenderer {
  public int getCharWidth(final char c) {
    return 0;
  }

  public int drawString(final String s, final int x, final int y, final int c) {
    return c;
  }
}
