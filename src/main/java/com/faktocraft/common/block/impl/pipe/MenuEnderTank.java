package com.faktocraft.common.block.impl.pipe;

import net.minecraft.world.phys.Vec3;
import com.faktocraft.common.registries.PipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class MenuEnderTank extends AbstractContainerMenu {

  private final BlockEntityEnderTank tank;

  public MenuEnderTank(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    super(PipeRegistry.ENDER_TANK_MENU, windowId);
    this.tank = level.getBlockEntity(pos) instanceof BlockEntityEnderTank found ? found : null;
  }

  @Nullable
  public BlockEntityEnderTank getTank() {
    return tank;
  }

  @Override
  public ItemStack quickMoveStack(Player player, int index) {
    return ItemStack.EMPTY;
  }

  @Override
  public boolean stillValid(Player player) {
    return tank != null && !tank.isRemoved()
        && player.distanceToSqr(Vec3.atCenterOf(tank.getBlockPos())) <= 64.0;
  }
}
