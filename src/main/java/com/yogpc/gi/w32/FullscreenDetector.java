package com.yogpc.gi.w32;

import java.lang.reflect.Field;

import org.lwjgl.opengl.Display;

public class FullscreenDetector {
  private static final Field cwd;
  private static final Field gwd;
  static {
    Field f = null;
    Field m = null;
    try {
      Class<?> t = Display.class.getClassLoader().loadClass("org.lwjgl.opengl.WindowsDisplay");
      f = t.getDeclaredField("current_display");
      f.setAccessible(true);
      m = t.getDeclaredField("hwnd");
      m.setAccessible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
    cwd = f;
    gwd = m;
  }

  public static final void update() {
    try {
      gwd.getLong(cwd.get(null));
      // TODO
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
