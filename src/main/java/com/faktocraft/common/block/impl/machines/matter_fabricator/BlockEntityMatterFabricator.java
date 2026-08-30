package com.faktocraft.common.block.impl.machines.matter_fabricator;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.BlockEntityProgress;
import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.entity.block.IndRebBlockEntity;
import com.faktocraft.common.entity.slot.IndRebSlot;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.fluid.ModFluids;
import com.faktocraft.common.interfaces.entity.ITileSound;
import com.faktocraft.common.registries.machines.M4Registry;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;

public class BlockEntityMatterFabricator extends IndRebBlockEntity
    implements IEnergyBlock, ITileSound, com.faktocraft.common.interfaces.entity.ISupportUpgrades {

  public static final int AMPLIFIER_SLOT = 0;

  public static final int PROGRESS_TARGET = 1_000_000;
  public static final int SCRAP_AMPLIFIER = 5_000;
  public static final int SCRAP_BOX_AMPLIFIER = 45_000;
  public static final int AMPLIFIER_BONUS = 3;

  public final BlockEntityProgress progress = new BlockEntityProgress();
  public final BlockEntityProgress progressAmplifier = new BlockEntityProgress(0, 0);

  private int cachedOutput = 0;
  public final FluidStorage fluidMatterStorage = new FluidStorage(
      ModConfig.server().matter_fabricator_matter_capacity,
      fluidStack -> fluidStack.getFluid() == ModFluids.MATTER.still());

  private final LazyOptional<IFluidHandler> fluidHandlerCap = LazyOptional.of(() -> fluidMatterStorage);

  public BlockEntityMatterFabricator(BlockPos pos, BlockState state) {
    super(M4Registry.MATTER_FABRICATOR_BE, pos, state);
    createEnergyStorage(0, ModConfig.server().matter_fabricator_energy_capacity, EnergyType.RECEIVE,
        EnergyTier.VERY_HIGH);
    fluidMatterStorage.setChangeListener(this::setChanged);
  }

  private boolean isValidInput(ItemStack stack) {
    if (stack.isEmpty()) {
      return false;
    }
    return stack.getItem() == ModItems.SCRAP || stack.getItem() == ModItems.SCRAP_BOX;
  }

  @Override
  public int customEnergyReceiveTick() {

    return Math.round(3072 / getSpeedFactor());
  }

  @Override
  public java.util.List<com.faktocraft.common.enums.UpgradeType> getSupportedUpgrades() {
    return java.util.List.of(com.faktocraft.common.enums.UpgradeType.OVERCLOCKER,
        com.faktocraft.common.enums.UpgradeType.EFFICIENCY);
  }

  @Override
  public void tickWork(BlockState state) {
    boolean active = false;
    getEnergyStorage().updateConsumed(0);

    final ItemStack amplifierStack = getItemStackHandler().getStackInSlot(AMPLIFIER_SLOT);

    if (cachedOutput != fluidMatterStorage.getFluidAmount()) {
      cachedOutput = fluidMatterStorage.getFluidAmount();
      shouldUpdateState = true;
    }

    int produceRun = ModConfig.server().matter_fabricator_produce_run;
    if (fluidMatterStorage.getFluidAmount() + produceRun <= fluidMatterStorage.getCapacityMb()) {
      if (!amplifierStack.isEmpty() && progressAmplifier.getProgress() == 0) {
        progressAmplifier.setBoth(amplifierStack.getItem() == ModItems.SCRAP_BOX
            ? SCRAP_BOX_AMPLIFIER
            : SCRAP_AMPLIFIER);
        amplifierStack.shrink(1);
      }

      if (getEnergyStorage().energyStored() > 0 && progressAmplifier.getProgress() > 0) {
        if (progress.getProgress() == -1) {
          progress.setData(0, PROGRESS_TARGET);
        }

        float usageFactor = getEnergyUsageFactor();
        int remaining = (int) progress.getProgressMax() - (int) progress.getProgress();
        int energyToFinish = (int) Math.ceil(remaining * usageFactor / (1 + AMPLIFIER_BONUS));
        int maxEnergyCost = Math.min(
            Math.min(energyToFinish, getEnergyStorage().energyStored()),
            (int) progressAmplifier.getProgress());

        if (getEnergyStorage().consumeEnergy(maxEnergyCost, true) == maxEnergyCost
            && progress.getProgress() <= progress.getProgressMax()) {
          active = true;

          progressAmplifier.decProgress(maxEnergyCost);
          if (progressAmplifier.getProgress() <= 0) {
            progressAmplifier.setBoth(0);
          }

          progress.incProgress(Math.max(1,
              (int) ((maxEnergyCost + maxEnergyCost * (long) AMPLIFIER_BONUS) / usageFactor)));

          getEnergyStorage().consumeEnergy(maxEnergyCost, false);
          getEnergyStorage().updateConsumed(maxEnergyCost);
        }

        if (progress.getProgress() >= progress.getProgressMax()) {
          fluidMatterStorage.fill(new FluidStack(ModFluids.MATTER.still(), produceRun),
              IFluidHandler.FluidAction.EXECUTE);
          progress.setBoth(-1);
        }
      }
    }

    if (progress.changed()) {
      progress.clearChanged();
      shouldUpdateState = true;
    }
    if (progressAmplifier.changed()) {
      progressAmplifier.clearChanged();
      shouldUpdateState = true;
    }

    setActive(active);
  }

  @Override
  public void tickClient(BlockState state) {
    handleAmplifiedSound();
    super.tickClient(state);
  }

  private void handleAmplifiedSound() {
    if (progressAmplifier.getProgress() > 0) {
      if (canPlaySound() && !isRemoved()) {
        com.faktocraft.client.ExtraSoundHandler.ensurePlaying(ModSounds.MATTER_FABRICATOR_AMPLIFIED, getBlockPos());
      } else {
        com.faktocraft.client.ExtraSoundHandler.stop(getBlockPos());
      }
    } else if (getItemStackHandler().getStackInSlot(AMPLIFIER_SLOT).isEmpty()) {
      com.faktocraft.client.ExtraSoundHandler.stop(getBlockPos());
    }
  }

  @Override
  public void onBreakClient() {
    com.faktocraft.client.ExtraSoundHandler.stop(getBlockPos());
    super.onBreakClient();
  }

  @Override
  public ArrayList<IndRebSlot> addInventorySlot(ArrayList<IndRebSlot> slots) {
    slots.add(new IndRebSlot(AMPLIFIER_SLOT, 83, 50, InventorySlotType.INPUT, GuiSlotType.NORMAL, 82, 49));
    return super.addInventorySlot(slots);
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    if (slot == AMPLIFIER_SLOT) {
      return isValidInput(stack);
    }
    return false;
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable Direction side) {
    return true;
  }

  @Override
  public SoundEvent getSoundEvent() {
    return ModSounds.MATTER_FABRICATOR;
  }

  @Override
  public int topOffsetVertical() {
    return 18;
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
    CompoundTag progressTag = new CompoundTag();
    progress.save(progressTag);
    tag.put("progress", progressTag);

    CompoundTag progressAmplifierTag = new CompoundTag();
    progressAmplifier.save(progressAmplifierTag);
    tag.put("progressAmplifier", progressAmplifierTag);

    CompoundTag fluidTag = new CompoundTag();
    fluidMatterStorage.save(fluidTag);
    tag.put("fluidMatterStorage", fluidTag);

    super.saveAdditional(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    if (tag.contains("progress")) {
      progress.load(tag.getCompound("progress"));
    }
    if (tag.contains("progressAmplifier")) {
      progressAmplifier.load(tag.getCompound("progressAmplifier"));
    }
    if (tag.contains("fluidMatterStorage")) {
      fluidMatterStorage.load(tag.getCompound("fluidMatterStorage"));
    }
  }

  @Override
  public java.util.List<com.faktocraft.common.entity.block.FluidStorage> getGuiTanks() {
    return java.util.List.of(fluidMatterStorage);
  }
}
