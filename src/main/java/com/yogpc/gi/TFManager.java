package com.yogpc.gi;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import com.yogpc.gi.dummy.GuiEditSign;
import com.yogpc.gi.dummy.GuiScreenBook;
import com.yogpc.gi.w32.JNIHandler;

public class TFManager {
  private static WeakReference<Object> ref = new WeakReference<Object>(null);
  private static WeakHashMap<Object, Long> map = new WeakHashMap<Object, Long>();

  public static final void hookShowGui(final Object o) {
    map.clear();
    if (o instanceof GuiEditSign || o instanceof GuiScreenBook) {
      ref = new WeakReference<Object>(o);
      JNIHandler.linkIME();
    } else {
      ref = new WeakReference<Object>(null);
      JNIHandler.unlinkIME();
    }
  }

  public static final void hookFocuse(final Object o, final boolean f) {
    map.put(o, Long.valueOf(f ? System.nanoTime() : -1));
    Object r = f ? o : null;
    long l = -1;
    if (o == null)
      for (final Map.Entry<Object, Long> e : map.entrySet())
        if (l < e.getValue().longValue()) {
          r = e.getKey();
          l = e.getValue().longValue();
        }
    if (r != null) {
      ref = new WeakReference<Object>(r);
      JNIHandler.linkIME();
    } else {
      ref = new WeakReference<Object>(null);
      JNIHandler.unlinkIME();
    }
  }

  public static final void updateWnd() {
    JNIHandler.updateHWnd();
    final Object o = ref.get();
    if (o != null)
      JNIHandler.linkIME();
    else
      JNIHandler.unlinkIME();
  }

  public static final boolean shouldKill() {
    return ref.get() == null;
  }
}
