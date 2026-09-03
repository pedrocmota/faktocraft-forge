package com.faktocraft.common.block.impl.machines.alloy_smelter;

import com.faktocraft.common.block.BlockMachine;
import com.faktocraft.common.interfaces.block.IHasMenu;
import com.faktocraft.common.util.BlockStateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockCoalAlloySmelter extends BlockMachine implements IHasMenu {

  public BlockCoalAlloySmelter(Properties properties) {
    super(properties);
  }

  @Override
  public AbstractContainerMenu getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory,
      Player player) {
    return new MenuCoalAlloySmelter(windowId, level, pos, playerInventory, player);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityCoalAlloySmelter(pos, state);
  }

  @Override
  public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    if (!state.getValue(BlockStateHelper.activeProperty)) {
      return;
    }
    double x = pos.getX() + 0.5D;
    double y = pos.getY();
    double z = pos.getZ() + 0.5D;
    if (random.nextDouble() < 0.1D) {
      level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
    }
    Direction direction = getDirection(state);
    Direction.Axis axis = direction.getAxis();
    double offset = random.nextDouble() * 0.6D - 0.3D;
    double dx = axis == Direction.Axis.X ? direction.getStepX() * 0.52D : offset;
    double dy = random.nextDouble() * 6.0D / 16.0D;
    double dz = axis == Direction.Axis.Z ? direction.getStepZ() * 0.52D : offset;
    level.addParticle(ParticleTypes.SMOKE, x + dx, y + dy, z + dz, 0.0D, 0.0D, 0.0D);
    level.addParticle(ParticleTypes.FLAME, x + dx, y + dy, z + dz, 0.0D, 0.0D, 0.0D);
  }
}
