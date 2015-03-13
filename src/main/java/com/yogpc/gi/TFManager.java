package com.yogpc.gi;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import com.yogpc.gi.dummy.GuiEditSign;
import com.yogpc.gi.dummy.GuiScreenBook;
import com.yogpc.gi.w32.JNIHandler;

public class TFManager {
  private static WeakReference<Handler> cur = new WeakReference<Handler>(null);
  private static Map<GTFHandler, Long> map = new WeakHashMap<GTFHandler, Long>();

  public static final void hookShowGui(final Object o) {
    map.clear();
    if (o instanceof GuiEditSign || o instanceof GuiScreenBook) {
      // TODO Handler for GuiEditSign and GuiScreenBook
      cur = new WeakReference<Handler>(null);
      JNIHandler.linkIME();
    } else {
      cur = new WeakReference<Handler>(null);
      JNIHandler.unlinkIME();
    }
  }

  public static final void hookFocuse(final GTFHandler o, final boolean fc, final boolean en) {
    map.put(o, Long.valueOf(fc && en ? System.nanoTime() : -1));
    GTFHandler r = null;
    long l = -1;
    for (final Map.Entry<GTFHandler, Long> e : map.entrySet())
      if (l < e.getValue().longValue()) {
        r = e.getKey();
        l = e.getValue().longValue();
      }
    if (r != null) {
      cur = new WeakReference<Handler>(r);
      JNIHandler.linkIME();
    } else {
      cur = new WeakReference<Handler>(null);
      JNIHandler.unlinkIME();
    }
  }

  public static final void updateWnd() {
    JNIHandler.updateHWnd();
    if (cur.get() != null)
      JNIHandler.linkIME();
    else
      JNIHandler.unlinkIME();
  }

  public static final boolean shouldKill() {
    return cur.get() == null;
  }

  public static final void pushComposition(final char[] c, final byte[] b, final int p) {
    final Handler i = cur.get();
    if (i != null)
      i.pushComposition(c, b, p);
  }

  public static final void pushResult(final String sn) {
    final Handler i = cur.get();
    if (i != null)
      i.pushResult(sn);
  }

  public static final void pushCandidate(final String[] s, final int c) {
    final Handler i = cur.get();
    if (i != null)
      i.pushCandidate(s, c);
  }

  public static final void pushStatus(final boolean b) {
    final Handler i = cur.get();
    if (i != null)
      i.pushStatus(b);
  }
}
