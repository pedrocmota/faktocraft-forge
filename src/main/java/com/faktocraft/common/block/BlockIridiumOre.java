package com.faktocraft.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlockIridiumOre extends BlockOre {

  public BlockIridiumOre(Properties properties) {
    super(properties);
  }

  @Override
  public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    if (random.nextInt(3) != 0) {
      return;
    }
    Direction direction = Direction.getRandom(random);
    BlockPos neighborPos = pos.relative(direction);
    if (level.getBlockState(neighborPos).isSolidRender(level, neighborPos)) {
      return;
    }
    double x = pos.getX() + 0.5 + (direction.getStepX() == 0
        ? random.nextDouble() - 0.5
        : direction.getStepX() * 0.55);
    double y = pos.getY() + 0.5 + (direction.getStepY() == 0
        ? random.nextDouble() - 0.5
        : direction.getStepY() * 0.55);
    double z = pos.getZ() + 0.5 + (direction.getStepZ() == 0
        ? random.nextDouble() - 0.5
        : direction.getStepZ() * 0.55);
    level.addParticle(ParticleTypes.GLOW, x, y, z, 0.0, 0.0, 0.0);
  }
}
