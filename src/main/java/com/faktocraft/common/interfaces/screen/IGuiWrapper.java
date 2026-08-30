package com.faktocraft.common.interfaces.screen;

import com.faktocraft.common.entity.block.IndRebBlockEntity;
import net.minecraft.resources.ResourceLocation;

public interface IGuiWrapper {
  IndRebBlockEntity getBlockEntity();

  ResourceLocation getGuiLocation();

  int getGuiLeft();

  int getGuiTop();
}
