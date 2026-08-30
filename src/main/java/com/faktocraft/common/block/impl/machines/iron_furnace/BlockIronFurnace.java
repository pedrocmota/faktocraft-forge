package com.faktocraft.common.block.impl.machines.iron_furnace;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockIronFurnace extends BlockMachine implements IHasMenu {

  public BlockIronFurnace(Properties properties) {
    super(properties);
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new BlockEntityIronFurnace(pos, state);
  }

  @Override
  public MenuIronFurnace getMenu(int windowId, Level level, BlockPos pos, Inventory playerInventory, Player player) {
    return new MenuIronFurnace(windowId, level, pos, playerInventory, player);
  }

  @Override
  public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    if (state.getValue(BlockStateHelper.activeProperty)) {
      double d0 = pos.getX() + 0.5D;
      double d1 = pos.getY();
      double d2 = pos.getZ() + 0.5D;
      if (random.nextDouble() < 0.1D) {
        level.playLocalSound(d0, d1, d2, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
      }

      Direction direction = getDirection(state);
      Direction.Axis axis = direction.getAxis();
      double d3 = 0.52D;
      double d4 = random.nextDouble() * 0.6D - 0.3D;
      double d5 = axis == Direction.Axis.X ? direction.getStepX() * d3 : d4;
      double d6 = random.nextDouble() * 6.0D / 16.0D;
      double d7 = axis == Direction.Axis.Z ? direction.getStepZ() * d3 : d4;
      level.addParticle(ParticleTypes.SMOKE, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
      level.addParticle(ParticleTypes.FLAME, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
    }
  }
}
