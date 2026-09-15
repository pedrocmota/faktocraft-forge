package com.faktocraft.common.block.impl.monitor;

import com.faktocraft.common.item.block.FaktocraftBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class StatusMonitorItem extends FaktocraftBlockItem {

  public StatusMonitorItem(Block block, Properties properties) {
    super(block, properties);
  }

  @Override
  protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
    if (!(getBlock() instanceof BlockStatusMonitor monitor)) {
      return false;
    }
    Level level = context.getLevel();
    BlockPos master = context.getClickedPos();
    Direction facing = BlockStatusMonitor.facingOf(state);
    if (!BlockStatusMonitor.canPlacePanel(level, master, facing, master)) {
      return false;
    }
    monitor.placePanel(level, master, facing);
    return true;
  }
}
