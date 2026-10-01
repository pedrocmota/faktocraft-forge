package com.faktocraft.common.block.impl.generators.combustion_generator;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.entity.slot.SlotBattery;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.fluid.ModFluids;
import com.faktocraft.common.interfaces.entity.ICooldown;
import com.faktocraft.common.interfaces.entity.IElectricSlot;
import com.faktocraft.common.interfaces.entity.ITileSound;
import com.faktocraft.common.registries.ModSounds;
import com.faktocraft.common.registries.machines.M1Registry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.state.BlockState;
import com.faktocraft.common.util.transfer.Capability;
import com.faktocraft.common.util.transfer.ForgeCapabilities;
import com.faktocraft.common.util.transfer.LazyOptional;
import com.faktocraft.common.util.transfer.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;

public class BlockEntityCombustionGenerator extends FaktocraftBlockEntity
    implements ICooldown, IEnergyBlock, ITileSound {

  public final FluidStorage fluidStorage = new FluidStorage(ModConfig.server().combustion_generator_fluid_capacity,
      fluidStack -> fluidStack.getFluid() == ModFluids.BIOGAS.still()
          || fluidStack.getFluid() == ModFluids.FUEL.still());

  private final LazyOptional<IFluidHandler> fluidHandlerCap = LazyOptional.of(() -> fluidStorage);

  private static final int LIT_AFTER_TICKS = 3;

  private int cachedFluid = 0;
  private int burnTicks = 0;
  private boolean refilling = false;

  public BlockEntityCombustionGenerator(BlockPos pos, BlockState state) {
    super(M1Registry.COMBUSTION_GENERATOR_BE, pos, state);
    createEnergyStorage(0, ModConfig.server().combustion_generator_energy_capacity, EnergyType.EXTRACT,
        EnergyTier.MEDIUM);
    fluidStorage.setChangeListener(this::setChanged);
  }

  @Override
  public ArrayList<IElectricSlot> addBatterySlot(ArrayList<IElectricSlot> slots) {
    slots.add(new SlotBattery(0, 152, 62, true));
    return super.addBatterySlot(slots);
  }

  @Override
  public void tickWork(BlockState state) {
    boolean active = false;
    boolean updateState = false;
    getEnergyStorage().updateGenerated(0);

    final int tickGenerate = fluidStorage.getFluid().isSame(ModFluids.BIOGAS.still())
        ? ModConfig.server().combustion_generator_biogas_tick_generate
        : ModConfig.server().combustion_generator_fuel_tick_generate;

    if (cachedFluid != fluidStorage.getFluidAmount()) {
      cachedFluid = fluidStorage.getFluidAmount();
      updateState = true;
    }

    if (!refilling && (long) getEnergyStorage().energyStored() * 100 <= (long) getEnergyStorage().maxEnergy()
        * ModConfig.server().generator_restart_threshold_percent) {
      refilling = true;
    }
    if (refilling) {
      int space = getEnergyStorage().generateEnergy(tickGenerate, true);
      if (space < tickGenerate) {
        refilling = false;
      } else if (fluidStorage.takeFluid(1, true) == 1) {
        fluidStorage.takeFluid(1, false);
        getEnergyStorage().generateEnergy(tickGenerate, false);
        getEnergyStorage().updateGenerated(tickGenerate);
        active = true;
        updateState = true;
      }
    }

    burnTicks = active ? Math.min(burnTicks + 1, LIT_AFTER_TICKS) : 0;
    boolean lit = burnTicks >= LIT_AFTER_TICKS;
    if (getActive() != lit) {
      updateState = true;
    }
    this.setActive(lit);

    if (updateState) {
      this.updateBlockState();
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
    tag.putBoolean("active", activeState);
    tag.putBoolean("refilling", refilling);
    super.saveAdditional(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    if (tag.contains("fluidStorage")) {
      fluidStorage.load(tag.getCompoundOrEmpty("fluidStorage"));
    }
    activeState = tag.getBooleanOr("active", false);
    refilling = tag.getBooleanOr("refilling", false);
  }

  @Override
  public int defaultGeneratorPriority() {
    return ModConfig.server().priority_combustion_generator;
  }

  @Override
  public SoundEvent getSoundEvent() {
    return ModSounds.COMBUSTION_GENERATOR;
  }

  @Override
  public boolean canExtractEnergyDir(@Nullable Direction side) {
    return true;
  }

  @Override
  public java.util.List<com.faktocraft.common.entity.block.FluidStorage> getGuiTanks() {
    return java.util.List.of(fluidStorage);
  }
}
