package com.faktocraft.common.block.impl.machines.canning_machine;

import com.faktocraft.common.block.impl.machines.FluidCellTankHelper;
import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.BlockEntityProgress;
import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.registries.ModSounds;
import com.faktocraft.common.interfaces.entity.ITileSound;
import com.faktocraft.common.entity.slot.FaktocraftSlot;
import com.faktocraft.common.entity.slot.SlotBattery;
import com.faktocraft.common.enums.CanningMachineMode;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.enums.UpgradeType;
import com.faktocraft.common.interfaces.entity.IElectricSlot;
import com.faktocraft.common.interfaces.entity.IMachineActions;
import com.faktocraft.common.interfaces.entity.ISupportUpgrades;
import com.faktocraft.common.registries.machines.M3Registry;
import com.faktocraft.common.util.EnergyCosts;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockEntityCanningMachine extends FaktocraftBlockEntity
    implements IEnergyBlock, ISupportUpgrades, IMachineActions.IModeSwitcher, ITileSound {

  public static final int CELL_UP = 0;
  public static final int CELL_DOWN = 1;

  public final FluidStorage fluidStorage = new FluidStorage(ModConfig.server().canning_machine_fluid_capacity);
  public final BlockEntityProgress progress = new BlockEntityProgress();

  protected CanningMachineMode mode = CanningMachineMode.FILL;
  private int cachedFluid = 0;

  private final LazyOptional<IFluidHandler> tankCap = LazyOptional.of(() -> fluidStorage);

  public BlockEntityCanningMachine(BlockPos pos, BlockState state) {
    super(M3Registry.CANNING_MACHINE_BLOCK_ENTITY, pos, state);
    createEnergyStorage(0, ModConfig.server().canning_machine_energy_capacity, EnergyType.RECEIVE, EnergyTier.LOW);
    initBatterySlots();
    fluidStorage.setChangeListener(this::setChanged);
  }

  @Override
  public ArrayList<FaktocraftSlot> addInventorySlot(ArrayList<FaktocraftSlot> slots) {
    slots.add(new FaktocraftSlot(CELL_UP, 127, 19, InventorySlotType.INPUT, GuiSlotType.NORMAL, 126, 18));
    slots.add(new FaktocraftSlot(CELL_DOWN, 127, 50, InventorySlotType.OUTPUT, GuiSlotType.NORMAL, 126, 49));
    return super.addInventorySlot(slots);
  }

  @Override
  public ArrayList<IElectricSlot> addBatterySlot(ArrayList<IElectricSlot> slots) {
    slots.add(new SlotBattery(0, 152, 62, false));
    return super.addBatterySlot(slots);
  }

  private boolean hasJob(ItemStack up, ItemStack down) {
    if (!FluidCellTankHelper.acceptsInput(up)) {
      return false;
    }
    if (mode == CanningMachineMode.FILL) {
      return FluidCellTankHelper.canDrainToCell(up, down, fluidStorage);
    }
    return FluidCellTankHelper.canFillFromCell(up, down, fluidStorage);
  }

  @Override
  public void tickWork(BlockState state) {
    boolean active = false;
    boolean updateState = false;
    getEnergyStorage().updateConsumed(0);

    if (cachedFluid != fluidStorage.getFluidAmount()) {
      cachedFluid = fluidStorage.getFluidAmount();
      updateState = true;
    }

    final ItemStack up = getItemStackHandler().getStackInSlot(CELL_UP);
    final ItemStack down = getItemStackHandler().getStackInSlot(CELL_DOWN);

    if (hasJob(up, down)) {
      if (progress.getProgress() == -1) {
        progress.setData(0, ModConfig.server().canning_machine_duration);
      }
      progress.rescaleMax(getSpeedFactor() * ModConfig.server().canning_machine_duration);
      int energyCost = EnergyCosts.perTick(ModConfig.server().canning_machine_tick_usage, getEnergyUsageFactor());

      if (getEnergyStorage().consumeEnergy(energyCost, true) == energyCost
          && progress.getProgress() <= progress.getProgressMax()) {
        active = true;
        progress.incProgress(1);
        getEnergyStorage().consumeEnergy(energyCost, false);
        getEnergyStorage().updateConsumed(energyCost);
      }

      if (progress.getProgress() >= progress.getProgressMax()) {
        boolean done = mode == CanningMachineMode.FILL
            ? FluidCellTankHelper.drainToCell(getItemStackHandler(), CELL_UP, CELL_DOWN, fluidStorage)
            : FluidCellTankHelper.fillFromCell(getItemStackHandler(), CELL_UP, CELL_DOWN, fluidStorage);
        if (done) {
          updateState = true;
        }
        progress.setBoth(-1);
      }
    } else {
      progress.setBoth(-1);
    }

    if (progress.changed()) {
      progress.clearChanged();
      updateState = true;
    }

    setActive(active);
    if (updateState) {
      updateBlockState();
    }
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    CompoundTag fluidTag = new CompoundTag();
    fluidStorage.save(fluidTag);
    tag.put("fluidStorage", fluidTag);
    CompoundTag progressTag = new CompoundTag();
    progress.save(progressTag);
    tag.put("progress", progressTag);
    tag.putInt("mode", mode.getId());
    tag.putBoolean("active", activeState);
    super.saveAdditional(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    if (tag.contains("fluidStorage")) {
      fluidStorage.load(tag.getCompound("fluidStorage"));
    }
    if (tag.contains("progress")) {
      progress.load(tag.getCompound("progress"));
    }
    this.mode = CanningMachineMode
        .getModeFromId(tag.contains("mode") ? tag.getInt("mode") : CanningMachineMode.FILL.getId());
    this.activeState = tag.getBoolean("active");
  }

  public CanningMachineMode getMode() {
    return mode;
  }

  @Override
  public void changeMode() {
    mode = mode == CanningMachineMode.FILL ? CanningMachineMode.EMPTY : CanningMachineMode.FILL;
    progress.setBoth(-1);
    progress.clearChanged();
    setActive(false);
    updateBlockState();
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable Direction side) {
    return true;
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    if (slot == CELL_UP) {
      return FluidCellTankHelper.acceptsInput(stack);
    }
    return false;
  }

  @Override
  public List<UpgradeType> getSupportedUpgrades() {
    return List.of(UpgradeType.OVERCLOCKER, UpgradeType.EFFICIENCY);
  }

  @NotNull
  @Override
  public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.FLUID_HANDLER) {
      if (isFrontSide(side)) {
        return LazyOptional.empty();
      }
      return tankCap.cast();
    }
    return super.getCapability(cap, side);
  }

  @Override
  public void setRemoved() {
    super.setRemoved();
    tankCap.invalidate();
  }

  @Override
  public java.util.List<com.faktocraft.common.entity.block.FluidStorage> getGuiTanks() {
    return java.util.List.of(fluidStorage);
  }

  @Override
  public SoundEvent getSoundEvent() {
    return ModSounds.CANNING_MACHINE;
  }
}
