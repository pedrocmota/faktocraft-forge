package com.faktocraft.common.entity.slot;

import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.interfaces.entity.ISlot;

public class FaktocraftSlot implements ISlot {

  private final int slotId;
  private final int xPosition;
  private final int yPosition;
  private final InventorySlotType inventorySlotType;
  private GuiSlotType guiSlotType;
  private final int guiX;
  private final int guiY;

  public FaktocraftSlot(int slotId, int xPosition, int yPosition, InventorySlotType inventorySlotType,
      GuiSlotType guiSlotType, int guiX, int guiY) {
    this.slotId = slotId;
    this.xPosition = xPosition;
    this.yPosition = yPosition;
    this.inventorySlotType = inventorySlotType;
    this.guiSlotType = guiSlotType;
    this.guiX = guiX;
    this.guiY = guiY;
  }

  @Override
  public int getSlotId() {
    return slotId;
  }

  @Override
  public int getXPosition() {
    return xPosition;
  }

  @Override
  public int getYPosition() {
    return yPosition;
  }

  @Override
  public InventorySlotType getInventorySlotType() {
    return inventorySlotType;
  }

  @Override
  public GuiSlotType guiSlotType() {
    return guiSlotType;
  }

  @Override
  public void setGuiSlotType(GuiSlotType type) {
    this.guiSlotType = type;
  }

  @Override
  public int getGuiX() {
    return guiX;
  }

  @Override
  public int getGuiY() {
    return guiY;
  }
}
