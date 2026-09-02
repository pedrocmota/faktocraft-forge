package com.faktocraft.common.container;

import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.interfaces.entity.ISlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SlotDisabled extends MachineSlot {

  public SlotDisabled(FaktocraftBlockEntity blockEntity, Container container, ISlot slotDescriptor) {
    super(blockEntity, container, slotDescriptor);
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return false;
  }

  @Override
  public boolean mayPickup(Player player) {
    return false;
  }

  @Override
  public int getMaxStackSize() {
    return 1;
  }
}
