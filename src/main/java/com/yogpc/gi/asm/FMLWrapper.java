package com.yogpc.gi.asm;

import java.util.Map;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

public class FMLWrapper extends DummyModContainer implements IFMLLoadingPlugin {
  private static final ModMetadata md = new ModMetadata();
  static {
    md.modId = "googleime";
    md.name = "GoogleIME";
  }

  public FMLWrapper() {
    super(md);
  }

  @Override
  public String[] getASMTransformerClass() {
    return new String[] {Asm.class.getName()};
  }

  @Override
  public String getModContainerClass() {
    return this.getClass().getName();
  }

  @Override
  public String getSetupClass() {
    return null;
  }

  @Override
  public void injectData(final Map<String, Object> data) {}

  @Override
  public String getAccessTransformerClass() {
    return null;
  }
}
