package com.faktocraft.common.interfaces.screen;

import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import net.minecraft.resources.Identifier;

public interface IGuiWrapper {
  FaktocraftBlockEntity getBlockEntity();

  Identifier getGuiLocation();

  int getGuiLeft();

  int getGuiTop();
}
