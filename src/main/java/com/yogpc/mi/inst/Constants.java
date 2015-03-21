package com.yogpc.mi.inst;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

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

  private static final String gson_on =
      "http://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.2.4/gson-2.2.4.jar";
  private static final File gson_off = new File(MINECRAFT_LIBRARIES, new StringBuilder()
      .append("com").append(File.separatorChar).append("google").append(File.separatorChar)
      .append("code").append(File.separatorChar).append("gson").append(File.separatorChar)
      .append("gson").append(File.separatorChar).append("2.2.4").append(File.separatorChar)
      .append("gson-2.2.4.jar").toString());

  // Wrapper for dependencies classpath solving
  public static final void main(final String[] arg) {
    try {
      if (!gson_off.exists()) {
        gson_off.getParentFile().mkdirs();
        final URL website = new URL(gson_on);
        final ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        final FileOutputStream fos = new FileOutputStream(gson_off);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
      }
      final URLClassLoader cl = (URLClassLoader) Constants.class.getClassLoader();
      final Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      addURL.setAccessible(true);
      addURL.invoke(cl, gson_off.toURI().toURL());
      Swing.show();
    } catch (final Exception e) {
      stackTraceDialog(e);
    } catch (final Error e) {
      stackTraceDialog(e);
    }
  }
}
