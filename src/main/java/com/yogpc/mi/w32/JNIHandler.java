package com.yogpc.mi.w32;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

import org.lwjgl.opengl.Display;

import com.yogpc.mi.TFManager;

public class JNIHandler {
  private static final String getOsBit() {
    String os = System.getProperty("sun.arch.data.model");
    if ("32".equals(os))
      return "32";
    if ("64".equals(os))
      return "64";
    os = System.getProperty("os.arch");
    if (os == null)
      return "32";
    if (os.endsWith("86"))
      return "32";
    if (os.endsWith("64"))
      return "64";
    return "32";
  }

  static {
    try {
      final InputStream is = JNIHandler.class.getResourceAsStream("MC-IME-" + getOsBit() + ".dll");
      final File t = File.createTempFile("MC-IME-", ".dll");
      t.deleteOnExit();
      final OutputStream os = new FileOutputStream(t);
      final byte[] buf = new byte[1024];
      int read;
      while ((read = is.read(buf)) != -1)
        os.write(buf, 0, read);
      os.close();
      is.close();
      System.load(t.getCanonicalPath());
    } catch (final Exception e) {
      e.printStackTrace();
    }
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

  public native static void linkIME();

  public native static void unlinkIME();

  private native static void setHWnd(long ptr);

  public static final void updateHWnd() {
    try {
      setHWnd(gwd.getLong(cwd.get(null)));
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  public static final boolean shouldKill() {
    return TFManager.shouldKill();
  }

  public static final void cbResult(final String s) {
    TFManager.pushResult(s);
  }

  public static final void cbComposition(final char[] c, final byte[] b, final int p) {
    TFManager.pushComposition(c, b, p);
  }

  public static final void cbCandidate(final String[] s, final int c) {
    TFManager.pushCandidate(s, c);
  }

  public static final void cbStatus(final boolean e) {
    TFManager.pushStatus(e);
  }
}
