package com.faktocraft.common.block.impl.monitor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public final class StatusSources {

  public static final String TAG_STATE = "state";

  private static final int DEFAULT_FLUID_COLOR = 0xFF3F76E4;

  private static final List<StatusSource> SOURCES = new ArrayList<>();
  private static ToIntFunction<Fluid> fluidColor = fluid -> DEFAULT_FLUID_COLOR;

  private StatusSources() {
  }

  public static void register(StatusSource source) {
    SOURCES.add(source);
  }

  public static void setFluidColorProvider(ToIntFunction<Fluid> provider) {
    fluidColor = provider;
  }

  public static int fluidColor(Fluid fluid) {
    return fluidColor.applyAsInt(fluid);
  }

  public static CompoundTag collect(ServerLevel level, BlockPos pos) {
    CompoundTag tag = new CompoundTag();
    BlockState state = level.getBlockState(pos);
    tag.put(TAG_STATE, NbtUtils.writeBlockState(state));
    BlockEntity blockEntity = level.getBlockEntity(pos);
    for (StatusSource source : SOURCES) {
      source.collect(level, pos, state, blockEntity, tag);
    }
    StatusBridges.collect(level, pos, state, blockEntity, tag);
    return tag;
  }

  public static BlockState readState(Level level, CompoundTag data) {
    if (!data.contains(TAG_STATE)) {
      return Blocks.AIR.defaultBlockState();
    }
    return NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), data.getCompound(TAG_STATE));
  }

  public static List<StatusLine> lines(BlockState state, CompoundTag data) {
    List<StatusLine> lines = new ArrayList<>();
    for (StatusSource source : SOURCES) {
      source.lines(state, data, lines);
    }
    return lines;
  }
}
