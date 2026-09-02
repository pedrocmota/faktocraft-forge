package com.faktocraft.common.entity.slot;

import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.interfaces.entity.IUpgradeSlot;

public class SlotUpgrade extends FaktocraftSlot implements IUpgradeSlot {

  public SlotUpgrade(int slotId, int xPosition, int yPosition) {
    super(slotId, xPosition, yPosition, InventorySlotType.UPGRADE, GuiSlotType.UPGRADE, xPosition - 1, yPosition - 1);
  }
}
