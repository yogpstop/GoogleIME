package com.yogpc.gi.w32;

import java.lang.reflect.Field;

import org.lwjgl.opengl.Display;

public class JNIHandler {
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

  native static boolean isOpenIME();

  native static void linkIME();

  native static void unlinkIME();

  native static void setHWnd(long ptr);
}
