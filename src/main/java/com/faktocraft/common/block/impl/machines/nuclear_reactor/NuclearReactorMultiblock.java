package com.faktocraft.common.block.impl.machines.nuclear_reactor;

import com.faktocraft.common.radiation.RadiationSources;
import com.faktocraft.common.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class NuclearReactorMultiblock {

  private static final int SIZE = ReactorPart.SIZE;

  private static boolean unforming;

  private NuclearReactorMultiblock() {
  }

  public static boolean tryForm(Level level, BlockPos placed) {
    if (level.isClientSide() || unforming) {
      return false;
    }
    BlockPos origin = findCube(level, placed, null);
    if (origin == null) {
      return false;
    }
    form(level, origin);
    return true;
  }

  public static boolean canComplete(Level level, BlockPos pos) {
    return findCube(level, pos, pos) != null;
  }

  @Nullable
  private static BlockPos findCube(Level level, BlockPos around, @Nullable BlockPos assumedReactor) {
    for (int dx = 1 - SIZE; dx <= 0; dx++) {
      for (int dy = 1 - SIZE; dy <= 0; dy++) {
        for (int dz = 1 - SIZE; dz <= 0; dz++) {
          BlockPos origin = around.offset(dx, dy, dz);
          if (isValidCube(level, origin, assumedReactor)) {
            return origin;
          }
        }
      }
    }
    return null;
  }

  private static boolean isValidCube(Level level, BlockPos origin, @Nullable BlockPos assumedReactor) {
    boolean hasReactor = false;
    for (int x = 0; x < SIZE; x++) {
      for (int y = 0; y < SIZE; y++) {
        for (int z = 0; z < SIZE; z++) {
          BlockPos pos = origin.offset(x, y, z);
          if (pos.equals(assumedReactor)) {
            hasReactor = true;
            continue;
          }
          BlockState state = level.getBlockState(pos);
          if (state.is(ModBlocks.NUCLEAR_REACTOR)) {
            if (!state.getValue(BlockNuclearReactor.PART).isSingle()) {
              return false;
            }
            hasReactor = true;
          } else if (!state.is(ModBlocks.ADVANCED_MACHINE_CASING)) {
            return false;
          }
        }
      }
    }
    return hasReactor;
  }

  private static void form(Level level, BlockPos origin) {
    int reactors = 0;
    BlockState base = ModBlocks.NUCLEAR_REACTOR.defaultBlockState();
    for (int x = 0; x < SIZE; x++) {
      for (int y = 0; y < SIZE; y++) {
        for (int z = 0; z < SIZE; z++) {
          BlockPos pos = origin.offset(x, y, z);
          boolean placed = false;
          if (level.getBlockState(pos).is(ModBlocks.NUCLEAR_REACTOR)) {
            placed = reactors == 0;
            reactors++;
          }
          if (level.getBlockEntity(pos) != null) {
            level.removeBlockEntity(pos);
          }
          level.setBlock(pos, base.setValue(BlockNuclearReactor.PART, ReactorPart.of(x, y, z))
              .setValue(BlockNuclearReactor.PLACED, placed), Block.UPDATE_ALL);
        }
      }
    }
    if (reactors > 1) {
      Block.popResource(level, origin.offset(SIZE / 2, SIZE, SIZE / 2),
          new ItemStack(ModBlocks.NUCLEAR_REACTOR, reactors - 1));
    }
    if (level instanceof ServerLevel serverLevel) {
      RadiationSources.get(serverLevel).add(serverLevel, origin.offset(SIZE / 2, SIZE / 2, SIZE / 2));
    }
  }

  public static void destroy(Level level, BlockPos origin) {
    if (level.isClientSide() || unforming) {
      return;
    }
    unforming = true;
    try {
      for (int x = 0; x < SIZE; x++) {
        for (int y = 0; y < SIZE; y++) {
          for (int z = 0; z < SIZE; z++) {
            BlockPos pos = origin.offset(x, y, z);
            if (level.getBlockState(pos).is(ModBlocks.NUCLEAR_REACTOR)) {
              level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
          }
        }
      }
    } finally {
      unforming = false;
    }
  }

  public static void unform(Level level, BlockPos pos, ReactorPart part) {
    if (level.isClientSide() || unforming || part.isSingle()) {
      return;
    }
    unforming = true;
    try {
      BlockPos origin = pos.offset(-part.x(), -part.y(), -part.z());
      if (level instanceof ServerLevel serverLevel) {
        RadiationSources.get(serverLevel).remove(serverLevel, origin.offset(SIZE / 2, SIZE / 2, SIZE / 2));
      }
      BlockState casing = ModBlocks.ADVANCED_MACHINE_CASING.defaultBlockState();
      for (int x = 0; x < SIZE; x++) {
        for (int y = 0; y < SIZE; y++) {
          for (int z = 0; z < SIZE; z++) {
            BlockPos other = origin.offset(x, y, z);
            if (other.equals(pos)) {
              continue;
            }
            BlockState state = level.getBlockState(other);
            if (state.is(ModBlocks.NUCLEAR_REACTOR) && !state.getValue(BlockNuclearReactor.PART).isSingle()) {
              boolean placed = state.getValue(BlockNuclearReactor.PLACED);
              level.setBlock(other, placed ? ModBlocks.NUCLEAR_REACTOR.defaultBlockState() : casing,
                  Block.UPDATE_ALL);
            }
          }
        }
      }
    } finally {
      unforming = false;
    }
  }
}
