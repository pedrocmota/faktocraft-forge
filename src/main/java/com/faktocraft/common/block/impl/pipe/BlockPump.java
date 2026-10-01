package com.faktocraft.common.block.impl.pipe;

import com.faktocraft.common.block.FaktocraftEntityBlock;
import com.faktocraft.common.interfaces.block.IHasMenu;
import com.faktocraft.common.interfaces.block.IStateActive;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockPump extends FaktocraftEntityBlock implements IStateActive, IHasMenu {

  public BlockPump(Properties properties) {
    super(properties);
  }

  @Override
  public AbstractContainerMenu getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory,
      Player player) {
    return new MenuPump(windowId, level, pos, playerInventory, player);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityPump(pos, state);
  }

  @Override
  protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level,
      BlockPos pos, boolean movedByPiston) {
    BlockEntityPump.clearTubeColumn(level, pos);
    super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
  }
}
