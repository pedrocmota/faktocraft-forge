package com.faktocraft.common.block.impl.machines.distillery;

import com.faktocraft.common.block.BlockElectricMachine;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.interfaces.block.IHasMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockDistillery extends BlockElectricMachine implements IHasMenu {

  public static final int TOWER_HEIGHT = 4;

  private static final net.minecraft.world.phys.shapes.VoxelShape BASE_OUTLINE = box(0, 0, 0, 16, 15.5, 16);

  public BlockDistillery(Properties properties) {
    super(EnergyTier.MEDIUM, properties);
  }

  @Override
  public net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,
      net.minecraft.world.level.BlockGetter level, BlockPos pos,
      net.minecraft.world.phys.shapes.CollisionContext context) {
    return BASE_OUTLINE;
  }

  @Override
  public net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(BlockState state,
      net.minecraft.world.level.BlockGetter level, BlockPos pos,
      net.minecraft.world.phys.shapes.CollisionContext context) {
    return net.minecraft.world.phys.shapes.Shapes.block();
  }

  private static java.util.List<BlockPos> guardPositions(BlockPos base) {
    java.util.ArrayList<BlockPos> list = new java.util.ArrayList<>();
    for (int dy = 0; dy <= TOWER_HEIGHT; dy++) {
      for (int dx = -1; dx <= 1; dx++) {
        for (int dz = -1; dz <= 1; dz++) {
          if (dx != 0 || dz != 0) {
            list.add(base.offset(dx, dy, dz));
          }
        }
      }
    }
    return list;
  }

  public static void healStructure(Level level, BlockPos base) {
    for (int i = 1; i <= TOWER_HEIGHT + 1; i++) {
      BlockPos towerPos = base.above(i);
      if (level.getBlockState(towerPos).isAir()) {
        level.setBlock(towerPos, DistilleryRegistry.DISTILLERY_TOWER.defaultBlockState()
            .setValue(BlockDistilleryTower.SEGMENT, i), 3);
      }
    }
    for (BlockPos guardPos : guardPositions(base)) {
      if (level.getBlockState(guardPos).isAir()) {
        level.setBlock(guardPos, DistilleryRegistry.DISTILLERY_GUARD.defaultBlockState(), 3);
      }
    }
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    BlockPos pos = context.getClickedPos();
    for (int i = 1; i <= TOWER_HEIGHT + 1; i++) {
      if (!context.getLevel().getBlockState(pos.above(i)).isAir()) {
        return null;
      }
    }
    for (BlockPos guardPos : guardPositions(pos)) {
      BlockState around = context.getLevel().getBlockState(guardPos);
      boolean conduitOk = guardPos.getY() - pos.getY() < TOWER_HEIGHT
          && BlockDistilleryGuard.isConduit(around.getBlock());
      if (!around.isAir() && !conduitOk) {
        return null;
      }
    }
    return super.getStateForPlacement(context);
  }

  @Override
  public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
      ItemStack stack) {
    super.setPlacedBy(level, pos, state, placer, stack);
    if (!level.isClientSide()) {
      for (int i = 1; i <= TOWER_HEIGHT + 1; i++) {
        BlockPos towerPos = pos.above(i);
        if (level.getBlockState(towerPos).isAir()) {
          level.setBlock(towerPos, DistilleryRegistry.DISTILLERY_TOWER.defaultBlockState()
              .setValue(BlockDistilleryTower.SEGMENT, i), 3);
        }
      }
      for (BlockPos guardPos : guardPositions(pos)) {
        if (level.getBlockState(guardPos).isAir()) {
          level.setBlock(guardPos, DistilleryRegistry.DISTILLERY_GUARD.defaultBlockState(), 3);
        }
      }
    }
  }

  @Override
  protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level,
      BlockPos pos, boolean movedByPiston) {
    for (int i = 1; i <= TOWER_HEIGHT + 1; i++) {
      BlockPos towerPos = pos.above(i);
      if (level.getBlockState(towerPos).is(DistilleryRegistry.DISTILLERY_TOWER)) {
        level.removeBlock(towerPos, false);
      }
    }
    for (BlockPos guardPos : guardPositions(pos)) {
      if (level.getBlockState(guardPos).is(DistilleryRegistry.DISTILLERY_GUARD)) {
        level.removeBlock(guardPos, false);
      }
    }
    super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
  }

  static boolean isLit(BlockState state) {
    return state.hasProperty(com.faktocraft.common.util.BlockStateHelper.activeProperty)
        && state.getValue(com.faktocraft.common.util.BlockStateHelper.activeProperty);
  }

  @Override
  public AbstractContainerMenu getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory,
      Player player) {
    return new MenuDistillery(windowId, level, pos, playerInventory, player);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityDistillery(pos, state);
  }
}
