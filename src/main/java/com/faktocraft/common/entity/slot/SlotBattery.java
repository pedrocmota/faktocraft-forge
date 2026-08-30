package com.faktocraft.common.entity.slot;

import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;

public class SlotBattery extends SlotElectric {

  public SlotBattery(int slotId, int xPosition, int yPosition, boolean charging) {
    super(slotId, xPosition, yPosition, InventorySlotType.BATTERY, GuiSlotType.BATTERY, charging);
  }
}
