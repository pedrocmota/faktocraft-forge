package com.faktocraft.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import java.util.EnumMap;
import java.util.Map;

public final class NeighborFluidCache {

  private final BlockEntity owner;
  private final Map<Direction, Entry> cache = new EnumMap<>(Direction.class);

  private record Entry(LazyOptional<IFluidHandler> cap, net.minecraft.world.level.block.state.BlockState state) {
  }

  public NeighborFluidCache(BlockEntity owner) {
    this.owner = owner;
  }

  @Nullable
  public IFluidHandler get(Direction direction) {
    Level level = owner.getLevel();
    if (level == null) {
      return null;
    }
    BlockPos neighborPos = owner.getBlockPos().relative(direction);

    net.minecraft.world.level.block.state.BlockState currentState = level.getBlockState(neighborPos);
    Entry cached = cache.get(direction);
    if (cached != null && cached.cap().isPresent() && cached.state() == currentState) {
      return cached.cap().orElse(null);
    }
    BlockEntity neighbor = level.getBlockEntity(neighborPos);
    if (neighbor != null) {
      LazyOptional<IFluidHandler> cap = neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER,
          direction.getOpposite());
      if (cap.isPresent()) {
        cache.put(direction, new Entry(cap, currentState));
        return cap.orElse(null);
      }
    }
    cache.remove(direction);
    return null;
  }
}
