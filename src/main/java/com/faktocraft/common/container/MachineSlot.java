package com.faktocraft.common.container;

import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.interfaces.entity.ISlot;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MachineSlot extends Slot {

  protected final FaktocraftBlockEntity blockEntity;
  protected final ISlot slotDescriptor;
  private boolean active = true;

  public MachineSlot(FaktocraftBlockEntity blockEntity, Container container, ISlot slotDescriptor) {
    super(container, slotDescriptor.getSlotId(), slotDescriptor.getXPosition(), slotDescriptor.getYPosition());
    this.blockEntity = blockEntity;
    this.slotDescriptor = slotDescriptor;
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return blockEntity.isItemValidForSlot(getContainerSlot(), stack);
  }

  @Override
  public int getMaxStackSize() {
    return Math.min(super.getMaxStackSize(), blockEntity.getCustomSlotLimit(getContainerSlot()));
  }

  @Override
  public int getMaxStackSize(ItemStack stack) {
    return Math.min(getMaxStackSize(), stack.getMaxStackSize());
  }

  @Override
  public boolean isActive() {
    return active;
  }
}
