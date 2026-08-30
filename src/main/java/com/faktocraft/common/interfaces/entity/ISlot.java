package com.faktocraft.common.interfaces.entity;

import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;

public interface ISlot {
  int getSlotId();

  int getXPosition();

  int getYPosition();

  InventorySlotType getInventorySlotType();

  GuiSlotType guiSlotType();

  void setGuiSlotType(GuiSlotType type);

  int getGuiX();

  int getGuiY();
}
