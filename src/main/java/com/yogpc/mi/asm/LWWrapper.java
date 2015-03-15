package com.yogpc.mi.asm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class LWWrapper implements ITweaker {
  private List<String> args;
  private static boolean standalone = true;
  private static String target = null;

  @Override
  public void acceptOptions(final List<String> arg, final File gameDir, final File assetsDir,
      final String profile) {
    this.args = new ArrayList<String>(arg);
    this.args.add("--gameDir");
    this.args.add(gameDir.getAbsolutePath());
    this.args.add("--assetsDir");
    this.args.add(assetsDir.getAbsolutePath());
    this.args.add("--version");
    this.args.add(profile);
    final int l = this.args.indexOf("--target");
    if (-1 < l && l + 1 < this.args.size())
      target = this.args.get(l + 1);
    standalone = this.args.remove("--mc_ime-standalone");
  }

  @Override
  public void injectIntoClassLoader(final LaunchClassLoader classLoader) {
    classLoader.registerTransformer(Asm.class.getName());
  }

  @Override
  public String getLaunchTarget() {
    return target != null ? target : "net.minecraft.client.main.Main";
  }

  @Override
  public String[] getLaunchArguments() {
    return standalone ? this.args.toArray(new String[this.args.size()]) : new String[0];
  }

}
