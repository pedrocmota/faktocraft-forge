package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.common.block.BlockMachine;
import com.faktocraft.common.interfaces.block.IHasMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockAssemblyTable extends BlockMachine implements IHasMenu {

  public BlockAssemblyTable(Properties properties) {
    super(properties);
  }

  @Override
  public AbstractContainerMenu getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory,
      Player player) {
    return new MenuAssemblyTable(windowId, level, pos, playerInventory, player);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityAssemblyTable(pos, state);
  }

  @Override
  public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
    super.onPlace(state, level, pos, oldState, isMoving);
    if (!level.isClientSide()) {
      LogisticsCores.markDirtyNear(level, pos);
    }
  }

  @Override
  protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos,
      boolean movedByPiston) {
    LogisticsCores.markDirtyNear(level, pos);
    super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
  }
}
