package com.faktocraft.common.container;

import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.interfaces.entity.ISlot;
import com.faktocraft.common.interfaces.item.IUpgradeItem;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class SlotUpgradeMenu extends MachineSlot {

  public SlotUpgradeMenu(FaktocraftBlockEntity blockEntity, Container container, ISlot slotDescriptor) {
    super(blockEntity, container, slotDescriptor);
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return stack.getItem() instanceof IUpgradeItem upgradeItem
        && blockEntity.getSupportedUpgrades().contains(upgradeItem.getUpgradeType());
  }

  @Override
  public int getMaxStackSize() {
    return 1;
  }

  @Override
  public int getMaxStackSize(ItemStack stack) {
    return 1;
  }
}
