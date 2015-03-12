package com.yogpc.gi.w32;

import java.lang.reflect.Field;

import org.lwjgl.opengl.Display;

import com.yogpc.gi.TFManager;

public class JNIHandler {
  static {
    System.loadLibrary("MC-IME");
  }
  private static final Field cwd;
  private static final Field gwd;
  static {
    Field f = null;
    Field m = null;
    try {
      final Class<?> t =
          Display.class.getClassLoader().loadClass("org.lwjgl.opengl.WindowsDisplay");
      f = t.getDeclaredField("current_display");
      f.setAccessible(true);
      m = t.getDeclaredField("hwnd");
      m.setAccessible(true);
    } catch (final Exception e) {
      e.printStackTrace();
    }
    cwd = f;
    gwd = m;
  }

  public static final void updateHWnd() {
    try {
      setHWnd(gwd.getLong(cwd.get(null)));
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  public native static boolean isOpenIME();

  public native static void linkIME();

  public native static void unlinkIME();

  private native static void setHWnd(long ptr);

  public static final void cbResult(final String s) {
    System.out.println("cbResult");
    System.out.println(s);
  }

  public static final void cbComposition(final char[] c, final byte[] b) {
    System.out.println("cbComposition");
    if (c != null) {
      System.out.println(c.length);
      System.out.println(c);
    }
    if (b != null) {
      System.out.println(b.length);
      System.out.println(b);
    }
  }

  public static final void cbCandidate(final String[] s, final int curCand, final int showFrom,
      final int showSize) {
    System.out.println("cbCandidate");
    System.out.println(s);
    System.out.println(curCand);
    System.out.println(showFrom);
    System.out.println(showSize);
  }

  public static final boolean shouldKill() {
    return TFManager.shouldKill();
  }
}
