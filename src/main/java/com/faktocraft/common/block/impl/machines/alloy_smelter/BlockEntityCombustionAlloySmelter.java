package com.faktocraft.common.block.impl.machines.alloy_smelter;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.fluid.ModFluids;
import com.faktocraft.common.interfaces.entity.ITileSound;
import com.faktocraft.common.registries.ModSounds;
import com.faktocraft.common.registries.machines.M3Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class BlockEntityCombustionAlloySmelter extends AbstractBlockEntityAlloySmelter implements ITileSound {

  private static final float STEP = 2.0F;

  public final FluidStorage fluidStorage = new FluidStorage(
      ModConfig.server().combustion_alloy_smelter_fluid_capacity,
      fluidStack -> fluidStack.getFluid() == ModFluids.BIOGAS.still()
          || fluidStack.getFluid() == ModFluids.FUEL.still());

  private final LazyOptional<IFluidHandler> fluidHandlerCap = LazyOptional.of(() -> fluidStorage);

  private int burnTicks = 0;
  private int cachedFluid = 0;

  public BlockEntityCombustionAlloySmelter(BlockPos pos, BlockState state) {
    super(M3Registry.COMBUSTION_ALLOY_SMELTER_BLOCK_ENTITY, pos, state);
    fluidStorage.setChangeListener(this::setChanged);
  }

  @Override
  public void tickWork(BlockState state) {
    boolean active = false;

    if (refreshWork()) {
      beginIfIdle();
      if (burnTicks <= 0 && fluidStorage.takeFluid(1, true) == 1) {
        boolean biogas = fluidStorage.getFluid().isSame(ModFluids.BIOGAS.still());
        fluidStorage.takeFluid(1, false);
        burnTicks += biogas ? ModConfig.server().combustion_alloy_smelter_biogas_ticks_per_mb
            : ModConfig.server().combustion_alloy_smelter_fuel_ticks_per_mb;
      }
      if (burnTicks > 0) {
        burnTicks--;
        active = true;
        progress.incProgress(STEP);
        if (finished()) {
          craft();
        }
      }
    } else {
      idle();
    }

    boolean fluidChanged = cachedFluid != fluidStorage.getFluidAmount();
    cachedFluid = fluidStorage.getFluidAmount();

    setActive(active);
    if (fluidChanged || progress.changed()) {
      progress.clearChanged();
      updateBlockState();
    }
  }

  @NotNull
  @Override
  public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.FLUID_HANDLER) {
      if (isFrontSide(side)) {
        return LazyOptional.empty();
      }
      return fluidHandlerCap.cast();
    }
    return super.getCapability(cap, side);
  }

  @Override
  public void setRemoved() {
    super.setRemoved();
    fluidHandlerCap.invalidate();
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    CompoundTag fluid = new CompoundTag();
    fluidStorage.save(fluid);
    tag.put("fluidStorage", fluid);
    tag.putInt("burnTicks", burnTicks);
    super.saveAdditional(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    if (tag.contains("fluidStorage")) {
      fluidStorage.load(tag.getCompound("fluidStorage"));
    }
    burnTicks = tag.getInt("burnTicks");
  }

  @Override
  public List<FluidStorage> getGuiTanks() {
    return List.of(fluidStorage);
  }

  @Override
  public SoundEvent getSoundEvent() {
    return ModSounds.ALLOY_SMELTER;
  }

  @Override
  public float getVolume() {
    return 0.35F;
  }
}
