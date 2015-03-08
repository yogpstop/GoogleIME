package com.yogpc.gi.asm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

public class LWWrapper implements ITweaker {
  private List<String> args;

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
  }

  @Override
  public void injectIntoClassLoader(final LaunchClassLoader classLoader) {
    classLoader.registerTransformer(Asm.class.getName());
  }

  @Override
  public String getLaunchTarget() {
    return "net.minecraft.client.main.Main";
  }

  @Override
  public String[] getLaunchArguments() {
    return this.args.toArray(new String[this.args.size()]);
  }

}
