package com.faktocraft.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class LegacyNbtBlockEntity extends BlockEntity {
  protected LegacyNbtBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  public void load(CompoundTag tag) {
  }

  protected void saveAdditional(CompoundTag tag) {
  }

  @Override
  public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    super.preRemoveSideEffects(pos, state);
    if (level != null && state.getBlock() instanceof com.faktocraft.common.block.IPreRemoveSideEffects hook) {
      hook.preRemoveSideEffects(state, level, pos, this);
    }
  }

  @Override
  protected final void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    NbtBridge.load(input, this::load);
  }

  @Override
  protected final void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    NbtBridge.with(NbtBridge.registries(), () -> NbtBridge.save(output, this::saveAdditional));
  }

  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    return NbtBridge.withResult(registries, this::getUpdateTag);
  }

  public CompoundTag getUpdateTag() {
    return new CompoundTag();
  }
}
