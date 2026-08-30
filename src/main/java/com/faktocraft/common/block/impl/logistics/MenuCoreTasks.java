package com.faktocraft.common.block.impl.logistics;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class MenuCoreTasks extends AbstractContainerMenu {

  private final BlockPos corePos;
  @Nullable
  private final BlockEntityLogisticsController core;

  public MenuCoreTasks(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(LogisticsRegistry.CORE_TASKS_MENU, windowId);
    this.corePos = pos;
    this.core = level.getBlockEntity(pos) instanceof BlockEntityLogisticsController found ? found : null;
  }

  public BlockPos getCorePos() {
    return corePos;
  }

  @Nullable
  public BlockEntityLogisticsController getCore() {
    return core;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean stillValid(Player player) {
    return core != null && !core.isRemoved()
        && player.distanceToSqr(corePos.getCenter()) <= 64.0;
  }
}
