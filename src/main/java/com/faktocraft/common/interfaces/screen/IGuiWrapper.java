package com.faktocraft.common.interfaces.screen;

import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import net.minecraft.resources.ResourceLocation;

public interface IGuiWrapper {
  FaktocraftBlockEntity getBlockEntity();

  ResourceLocation getGuiLocation();

  int getGuiLeft();

  int getGuiTop();
}
