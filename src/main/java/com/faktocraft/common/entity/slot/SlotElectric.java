package com.faktocraft.common.entity.slot;

import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.interfaces.entity.IElectricSlot;

public class SlotElectric extends FaktocraftSlot implements IElectricSlot {

  private final boolean charging;

  public SlotElectric(int slotId, int xPosition, int yPosition, InventorySlotType inventorySlotType,
      GuiSlotType guiSlotType, boolean charging) {
    super(slotId, xPosition, yPosition, inventorySlotType, guiSlotType, xPosition - 1, yPosition - 1);
    this.charging = charging;
  }

  @Override
  public boolean isCharging() {
    return charging;
  }
}
