package com.faktocraft.common.block.impl.machines.fermenter;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.BlockEntityProgress;
import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.entity.block.IndRebBlockEntity;
import com.faktocraft.common.registries.ModSounds;
import com.faktocraft.common.interfaces.entity.ITileSound;
import com.faktocraft.common.entity.slot.IndRebSlot;
import com.faktocraft.common.entity.slot.SlotBattery;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.enums.UpgradeType;
import com.faktocraft.common.fluid.ModFluids;
import com.faktocraft.common.interfaces.entity.IElectricSlot;
import com.faktocraft.common.interfaces.entity.ISupportUpgrades;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.machines.M3Registry;
import com.faktocraft.common.util.StackHandlerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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
import java.util.List;

public class BlockEntityFermenter extends IndRebBlockEntity implements IEnergyBlock, ISupportUpgrades, ITileSound {

  public static final int BIOGAS_PER_OP = 250;

  public static final int BIOMASS_PER_OP = 1000;

  public static final int DURATION_TICKS = 600;

  public static final int WASTE_EVERY_TICKS = 1400;

  public static final int MUD_PER_OP = 1;

  public static final int INPUT_SLOT = 0;

  public static final int WASTE_SLOT = 1;

  public final FluidStorage fluidInputStorage = new FluidStorage(
      ModConfig.server().fermenter_biomass_capacity, v -> v.getFluid() == ModFluids.BIOMASS.still());
  public final FluidStorage fluidOutputStorage = new FluidStorage(ModConfig.server().fermenter_biogas_capacity);

  private int cachedInput = 0;
  private int cachedOutput = 0;

  public final BlockEntityProgress progress = new BlockEntityProgress();
  public final BlockEntityProgress progressWaste = new BlockEntityProgress();
  public final BlockEntityProgress heatLevel = new BlockEntityProgress(0, 100);

  private final LazyOptional<IFluidHandler> fluidPortsCap = LazyOptional.of(
      () -> new com.faktocraft.common.block.impl.machines.MachineFluidPorts(
          java.util.List.of(fluidInputStorage), java.util.List.of(fluidOutputStorage)));

  public BlockEntityFermenter(BlockPos pos, BlockState state) {
    super(M3Registry.FERMENTER_BLOCK_ENTITY, pos, state);
    createEnergyStorage(0, ModConfig.server().fermenter_energy_capacity, EnergyType.RECEIVE, EnergyTier.HIGH);
    initBatterySlots();
    fluidInputStorage.setChangeListener(this::setChanged);
    fluidOutputStorage.setChangeListener(this::setChanged);
  }

  @Override
  public ArrayList<IndRebSlot> addInventorySlot(ArrayList<IndRebSlot> slots) {
    slots.add(new IndRebSlot(INPUT_SLOT, 13, 35, InventorySlotType.INPUT, GuiSlotType.NORMAL, 12, 34));
    slots.add(new IndRebSlot(WASTE_SLOT, 51, 73, InventorySlotType.OUTPUT, GuiSlotType.NORMAL, 50, 72));
    return super.addInventorySlot(slots);
  }

  @Override
  public ArrayList<IElectricSlot> addBatterySlot(ArrayList<IElectricSlot> slots) {
    slots.add(new SlotBattery(0, 152, 62, false));
    return super.addBatterySlot(slots);
  }

  @Override
  public void tickWork(BlockState state) {
    boolean active = false;
    boolean updateState = false;
    getEnergyStorage().updateConsumed(0);
    int heatCost = ModConfig.server().fermenter_heat_cost;

    final ItemStack inputStack = getItemStackHandler().getStackInSlot(INPUT_SLOT);
    final ItemStack wasteStack = getItemStackHandler().getStackInSlot(WASTE_SLOT);

    if (cachedInput != fluidInputStorage.getFluidAmount()) {
      cachedInput = fluidInputStorage.getFluidAmount();
      updateState = true;
    }

    if (cachedOutput != fluidOutputStorage.getFluidAmount()) {
      cachedOutput = fluidOutputStorage.getFluidAmount();
      updateState = true;
    }

    progress.rescaleMax(getSpeedFactor() * DURATION_TICKS);
    progressWaste.rescaleMax(getSpeedFactor() * WASTE_EVERY_TICKS);
    int energyCost = (int) (ModConfig.server().fermenter_tick_usage * getEnergyUsageFactor());

    boolean wasteRoom = wasteStack.isEmpty()
        || (wasteStack.is(ModItems.FERTILIZER) && wasteStack.getCount() < wasteStack.getMaxStackSize());

    if (fluidInputStorage.getFluidAmount() >= BIOMASS_PER_OP
        && inputStack.getCount() >= MUD_PER_OP
        && fluidOutputStorage.getFluidAmount() + BIOGAS_PER_OP <= fluidOutputStorage.getCapacityMb()
        && wasteRoom) {
      if (progress.getProgress() == -1) {
        progress.setData(0, DURATION_TICKS);
      }

      if (progressWaste.getProgress() == -1) {
        progressWaste.setData(0, WASTE_EVERY_TICKS);
      }

      if (getEnergyStorage().consumeEnergy(energyCost, true) == energyCost
          && progress.getProgress() <= progress.getProgressMax()) {
        active = true;
        progress.incProgress(1 + (heatLevel.getPercentProgress() / 100f));
        progressWaste.incProgress(1);

        getEnergyStorage().consumeEnergy(energyCost, false);
        getEnergyStorage().updateConsumed(energyCost);
      }

      if (progress.getProgress() >= progress.getProgressMax()) {
        StackHandlerHelper.shrinkInputStack(getItemStackHandler(), INPUT_SLOT, MUD_PER_OP);
        fluidInputStorage.takeFluid(BIOMASS_PER_OP, false);
        fluidOutputStorage.fillFluid(new FluidStack(ModFluids.BIOGAS.still(), BIOGAS_PER_OP), BIOGAS_PER_OP, false);

        progress.setBoth(-1);
      }

      if (progressWaste.getProgress() >= progressWaste.getProgressMax()) {
        if (wasteStack.isEmpty()) {
          getItemStackHandler().setStackInSlot(WASTE_SLOT, new ItemStack(ModItems.FERTILIZER));
        } else {
          wasteStack.grow(1);
        }
        progressWaste.setBoth(-1);
      }
    }

    if (progress.changed()) {
      progress.clearChanged();
      updateState = true;
    }

    if (progressWaste.changed()) {
      progressWaste.clearChanged();
      updateState = true;
    }

    if ((getRedstonePower() > 0 && getEnergyStorage().consumeEnergy(heatCost, true) >= heatCost) || active) {
      if (heatLevel.getProgress() < 100 && tickCounter == 20) {
        heatLevel.incProgress(0.2f);
        if (!active) {
          getEnergyStorage().consumeEnergy(heatCost, false);
        }
      }
    } else {
      if (heatLevel.getProgress() > 0 && tickCounter == 20) {
        heatLevel.decProgress(Math.min(heatLevel.getProgress(), 1));
      }
    }

    if (heatLevel.changed()) {
      heatLevel.clearChanged();
      updateState = true;
    }

    setActive(active);
    if (updateState) {
      updateBlockState();
    }
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    CompoundTag fluidInputTag = new CompoundTag();
    fluidInputStorage.save(fluidInputTag);
    tag.put("fluidInputStorage", fluidInputTag);
    CompoundTag fluidOutputTag = new CompoundTag();
    fluidOutputStorage.save(fluidOutputTag);
    tag.put("fluidOutputStorage", fluidOutputTag);
    CompoundTag progressTag = new CompoundTag();
    progress.save(progressTag);
    tag.put("progress", progressTag);
    tag.putBoolean("active", activeState);
    CompoundTag heatLevelTag = new CompoundTag();
    heatLevel.save(heatLevelTag);
    tag.put("heatLevel", heatLevelTag);
    CompoundTag progressWasteTag = new CompoundTag();
    progressWaste.save(progressWasteTag);
    tag.put("progressWaste", progressWasteTag);
    super.saveAdditional(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    if (tag.contains("fluidInputStorage")) {
      fluidInputStorage.load(tag.getCompound("fluidInputStorage"));
    }
    if (tag.contains("fluidOutputStorage")) {
      fluidOutputStorage.load(tag.getCompound("fluidOutputStorage"));
    }
    if (tag.contains("progress")) {
      progress.load(tag.getCompound("progress"));
    }
    this.activeState = tag.getBoolean("active");
    if (tag.contains("heatLevel")) {
      heatLevel.load(tag.getCompound("heatLevel"));
    }
    if (tag.contains("progressWaste")) {
      progressWaste.load(tag.getCompound("progressWaste"));
    }
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable Direction side) {
    return true;
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    return slot == INPUT_SLOT && (stack.is(ModItems.MUD_PILE) || stack.is(ModItems.SAWDUST)
        || stack.is(net.minecraft.world.item.Items.SUGAR));
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
      return fluidPortsCap.cast();
    }
    return super.getCapability(cap, side);
  }

  @Override
  public void setRemoved() {
    super.setRemoved();
    fluidPortsCap.invalidate();
  }

  @Override
  public java.util.List<com.faktocraft.common.entity.block.FluidStorage> getGuiTanks() {
    return java.util.List.of(fluidInputStorage, fluidOutputStorage);
  }

  @Override
  public SoundEvent getSoundEvent() {
    return ModSounds.FERMENTER;
  }
}
