package com.yogpc.mi.asm;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraft.launchwrapper.LogWrapper;

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
    LogWrapper.info("[MCIME] acceptOptions: %s", arg.toString());
    final int l = this.args.indexOf("--target");
    if (-1 < l && l + 1 < this.args.size())
      target = this.args.get(l + 1);
    standalone = this.args.remove("--mc_ime-standalone");
  }

  @Override
  public void injectIntoClassLoader(final LaunchClassLoader cl) {
    LogWrapper.info("[MCIME] injectIntoClassLoader: %s", cl.getClass().getName());
    cl.registerTransformer(Asm.class.getName());
  }

  @Override
  public String getLaunchTarget() {
    LogWrapper.info("[MCIME] getLaunchTarget: %s", target);
    return target != null ? target : "net.minecraft.client.main.Main";
  }

  @Override
  public String[] getLaunchArguments() {
    LogWrapper.info("[MCIME] getURLs(): %s", Arrays.toString(Launch.classLoader.getURLs()));
    LogWrapper.info("[MCIME] blackboard: %s", Launch.blackboard.toString());
    LogWrapper.info("[MCIME] getLaunchArguments: %b %s", Boolean.valueOf(standalone),
        this.args.toString());
    return standalone ? this.args.toArray(new String[this.args.size()]) : new String[0];
  }

}
