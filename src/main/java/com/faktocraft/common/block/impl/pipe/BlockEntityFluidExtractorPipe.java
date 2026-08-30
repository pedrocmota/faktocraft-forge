package com.faktocraft.common.block.impl.pipe;

import com.faktocraft.common.energy.EnergyLookup;
import com.faktocraft.common.energy.interfaces.IEnergy;
import com.faktocraft.common.energy.provider.EnergyCore;
import com.faktocraft.common.registries.PipeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockEntityFluidExtractorPipe extends BlockEntityFluidPipe
    implements IExtractorPipe, EnergyLookup.Provider {

  private static final int MB_PER_PULSE = 50;

  private final PipeExtractor extractor = new PipeExtractor(this);

  public BlockEntityFluidExtractorPipe(BlockPos pos, BlockState state) {
    super(PipeRegistry.FLUID_EXTRACTOR_PIPE_BLOCK_ENTITY, pos, state);
  }

  @Override
  public PipeExtractor extractor() {
    return extractor;
  }

  @Override
  @Nullable
  public IEnergy getEnergyLookup(@Nullable Direction side) {
    return extractor.energy();
  }

  @Override
  public void tick() {
    super.tick();
    if (level != null && !level.isClientSide()) {
      extractor.tick(() -> extractPulse(MB_PER_PULSE));
    }
  }

  @Override
  public void onLoad() {
    super.onLoad();
    if (level != null && !level.isClientSide()) {
      EnergyCore.get(level).addEnergyBlock(worldPosition);
    }
  }

  public void onBroken(BlockPos pos) {
    if (level != null && !level.isClientSide()) {
      extractor.dropUpgrades(level, pos);
      EnergyCore.get(level).removeEnergyBlock(pos);
    }
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    extractor.save(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    extractor.load(tag);
  }
}
