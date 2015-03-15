package com.yogpc.gi.inst;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class Constants {

  private static final String osn = System.getProperty("os.name").toLowerCase();

  private static final File getDirectory(final String name) {
    final String home = System.getProperty("user.home", ".");
    File wdir;
    if (osn.startsWith("win")) {
      final String appdata = System.getenv("APPDATA");
      wdir = new File(appdata != null ? appdata : home, "." + name);
    } else if (osn.startsWith("mac"))
      wdir = new File(home, "Library/Application Support/" + name);
    else if (osn.startsWith("linux") || osn.startsWith("unix") || osn.startsWith("sunos"))
      wdir = new File(home, "." + name);
    else
      wdir = new File(home, name);
    return wdir;
  }

  public static final File MINECRAFT_DIR = getDirectory("minecraft");
  public static final File MINECRAFT_LIBRARIES = new File(MINECRAFT_DIR, "libraries");
  public static final File MINECRAFT_VERSIONS = new File(MINECRAFT_DIR, "versions");

  static {
    try {
      final URLClassLoader cl = (URLClassLoader) Constants.class.getClassLoader();
      final Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      addURL.setAccessible(true);
      addURL.invoke(cl, new File(MINECRAFT_DIR, "launcher.jar").toURI().toURL());
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  // Wrapper for dependencies classpath solving
  public static final void main(final String[] arg) {
    Swing.main(arg);
  }
}
