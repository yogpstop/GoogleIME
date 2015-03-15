package com.yogpc.mi.inst;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class Main {
  private static final class V {
    final String jar;
    final List<String> args;
    final int lv;

    V(final JsonObject je, final String id) {
      this.args =
          new ArrayList<String>(
              Arrays.asList(je.get("minecraftArguments").getAsString().split(" ")));
      final String te = je.get("mainClass").getAsString();
      if (!"net.minecraft.client.main.Main".equals(te)
          && !"net.minecraft.launchwrapper.Launch".equals(te)) {
        this.args.add("--target");
        this.args.add(te);
      }
      this.args.add("--tweakClass");
      this.args.add("com.yogpc.mi.asm.LWWrapper");
      final JsonElement tl = je.get("minimumLauncherVersion");
      if (tl == null)
        this.lv = -1;
      else
        this.lv = tl.getAsInt();
      final JsonElement tj = je.get("jar");
      if (tj == null)
        this.jar = id;
      else
        this.jar = tj.getAsString();
    }
  }

  private static final Map<String, V> vs = new HashMap<String, V>();
  private static final Map<String, String> ps = new HashMap<String, String>();

  static final String[] getVersions() {
    return vs.keySet().toArray(new String[vs.size()]);
  }

  static final String[] getProfiles() {
    return ps.keySet().toArray(new String[ps.size()]);
  }

  static final String convert(final String k) {
    return ps.get(k);
  }

  static {
    final File[] d = Constants.MINECRAFT_VERSIONS.listFiles();
    for (final File f : d)
      try {
        final String id = f.getName();
        final File json = new File(f, id + ".json");
        if (!json.exists())
          continue;
        final InputStream is = new FileInputStream(json);
        final Reader r = new InputStreamReader(is);
        final JsonReader jr = new JsonReader(r);
        final JsonObject je = Streams.parse(jr).getAsJsonObject();
        jr.close();
        r.close();
        is.close();
        if (!id.equals(je.get("id").getAsString()))
          continue;
        vs.put(id, new V(je, id));
      } catch (final Exception e) {
        e.printStackTrace();
      }
    try {
      final File lp = new File(Constants.MINECRAFT_DIR, "launcher_profiles.json");
      final InputStream is = new FileInputStream(lp);
      final Reader r = new InputStreamReader(is);
      final JsonReader jr = new JsonReader(r);
      final JsonObject je = Streams.parse(jr).getAsJsonObject();
      jr.close();
      r.close();
      is.close();
      for (final Map.Entry<String, JsonElement> e : je.get("profiles").getAsJsonObject().entrySet()) {
        final JsonObject eo = e.getValue().getAsJsonObject();
        final String id = e.getKey();
        final JsonElement vid = eo.get("lastVersionId");
        if (!id.equals(eo.get("name").getAsString()) || vid == null)
          continue;
        ps.put(id, vid.getAsString());
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  static final void installJson(final String pn, final String to) {
    final V pv = vs.get(pn);
    final JsonObject out = new JsonObject();
    out.add("id", new JsonPrimitive(to));
    out.add("time", new JsonPrimitive("2014-11-24T23:13:31+09:00"));// FIXME
    out.add("releaseTime", new JsonPrimitive("2014-11-24T23:13:31+09:00"));// FIXME
    out.add("type", new JsonPrimitive("release"));
    final StringBuilder sb = new StringBuilder();
    for (final String s : pv.args)
      sb.append(s).append(' ');
    sb.deleteCharAt(sb.length() - 1);
    out.add("minecraftArguments", new JsonPrimitive(sb.toString()));
    final JsonArray lib = new JsonArray();
    JsonObject tmp = new JsonObject();
    tmp.add("name", new JsonPrimitive("com.yogpc.mi:MCIME:{version}"));
    lib.add(tmp);
    tmp = new JsonObject();
    tmp.add("name", new JsonPrimitive("org.ow2.asm:asm-all:5.0.3"));
    lib.add(tmp);
    tmp = new JsonObject();
    tmp.add("name", new JsonPrimitive("net.minecraft:launchwrapper:1.11"));
    lib.add(tmp);
    out.add("libraries", lib);
    out.add("mainClass", new JsonPrimitive("net.minecraft.launchwrapper.Launch"));
    if (pv.lv > -1)
      out.add("minimumLauncherVersion", new JsonPrimitive(Integer.valueOf(pv.lv)));
    out.add("inheritsFrom", new JsonPrimitive(pn));
    out.add("jar", new JsonPrimitive(pv.jar));
    try {
      final File od = new File(Constants.MINECRAFT_VERSIONS, to);
      od.mkdirs();
      final File of = new File(od, to + ".json");
      final OutputStream os = new FileOutputStream(of);
      final Writer w = new OutputStreamWriter(os);
      final JsonWriter jw = new JsonWriter(w);
      Streams.write(out, jw);
      jw.close();
      w.close();
      os.close();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  static final void installLib() {
    final StringBuilder sb = new StringBuilder();
    sb.append("com").append(File.separatorChar);
    sb.append("yogpc").append(File.separatorChar);
    sb.append("mi").append(File.separatorChar);
    sb.append("MCIME").append(File.separatorChar);
    sb.append("{version}");
    final File ld = new File(Constants.MINECRAFT_LIBRARIES, sb.toString());
    ld.mkdirs();
    final String fn = "MCIME-{version}.jar";
    try {
      final InputStream is =
          Main.class.getProtectionDomain().getCodeSource().getLocation().openStream();
      final OutputStream os = new FileOutputStream(new File(ld, fn));
      final byte[] buf = new byte[4096];
      int read;
      while ((read = is.read(buf)) > -1)
        os.write(buf, 0, read);
      os.close();
      is.close();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  static final void overrideProfile(final String pro, final String vid) {
    try {
      final File lp = new File(Constants.MINECRAFT_DIR, "launcher_profiles.json");
      final InputStream is = new FileInputStream(lp);
      final Reader r = new InputStreamReader(is);
      final JsonReader jr = new JsonReader(r);
      final JsonObject je = Streams.parse(jr).getAsJsonObject();
      jr.close();
      r.close();
      is.close();
      for (final Map.Entry<String, JsonElement> e : je.get("profiles").getAsJsonObject().entrySet()) {
        final JsonObject eo = e.getValue().getAsJsonObject();
        if (!e.getKey().equals(pro) || !e.getKey().equals(eo.get("name").getAsString()))
          continue;
        eo.add("lastVersionId", new JsonPrimitive(vid));
      }
      final OutputStream os = new FileOutputStream(lp);
      final Writer w = new OutputStreamWriter(os);
      final JsonWriter jw = new JsonWriter(w);
      Streams.write(je, jw);
      jw.close();
      w.close();
      os.close();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }
}
