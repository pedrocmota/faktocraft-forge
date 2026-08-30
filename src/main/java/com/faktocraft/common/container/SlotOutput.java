package com.faktocraft.common.container;

import com.faktocraft.common.entity.block.IndRebBlockEntity;
import com.faktocraft.common.interfaces.entity.IExpCollector;
import com.faktocraft.common.interfaces.entity.ISlot;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SlotOutput extends MachineSlot {

  public SlotOutput(IndRebBlockEntity blockEntity, Container container, ISlot slotDescriptor) {
    super(blockEntity, container, slotDescriptor);
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return false;
  }

  @Override
  public void onTake(Player player, ItemStack stack) {
    super.onTake(player, stack);
    if (!player.level().isClientSide() && blockEntity instanceof IExpCollector expCollector
        && !expCollector.hasExpButton()) {
      expCollector.collectExp(player);
    }
  }
}
