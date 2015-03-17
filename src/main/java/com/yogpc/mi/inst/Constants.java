package com.yogpc.mi.inst;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class Constants {
  private static final String osn = System.getProperty("os.name", "").toLowerCase();

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

  public static void stackTraceDialog(final Throwable t) {
    final StringWriter sw = new StringWriter();
    final PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    pw.close();
    try {
      sw.close();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
    final JPanel jp = new JPanel();
    final JTextArea jt = new JTextArea();
    jt.setText(sw.toString());
    jt.setEditable(false);
    final JScrollPane js = new JScrollPane(jt);
    jp.add(js);
    JOptionPane.showMessageDialog(null, jp, "Error", JOptionPane.ERROR_MESSAGE);
  }

  public static File detectLauncherJar() {
    final File f1 = new File(MINECRAFT_DIR, "launcher.jar");
    final String s2 = System.getenv("ProgramFiles");
    final String s3 = System.getenv("ProgramFiles(x86)");
    final File f2 =
        new File(s2 + File.separatorChar + "Minecraft" + File.separatorChar + "game"
            + File.separatorChar + "launcher.jar");
    final File f3 =
        new File(s3 + File.separatorChar + "Minecraft" + File.separatorChar + "game"
            + File.separatorChar + "launcher.jar");
    if (!f1.exists() && !f2.exists() && !f3.exists())
      throw new RuntimeException(f1.getPath() + f2.getPath() + f3.getPath());
    return f1.exists() ? f1 : f2.exists() ? f2 : f3.exists() ? f3 : null;
  }

  // Wrapper for dependencies classpath solving
  public static final void main(final String[] arg) {
    try {
      final URLClassLoader cl = (URLClassLoader) Constants.class.getClassLoader();
      final Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      addURL.setAccessible(true);
      addURL.invoke(cl, detectLauncherJar().toURI().toURL());
      Swing.show();
    } catch (final Exception e) {
      stackTraceDialog(e);
    } catch (final Error e) {
      stackTraceDialog(e);
    }
  }
}
