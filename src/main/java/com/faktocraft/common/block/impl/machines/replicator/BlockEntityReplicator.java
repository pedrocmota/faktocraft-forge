package com.faktocraft.common.block.impl.machines.replicator;

import com.faktocraft.common.capabilities.scan_result.ScannerResult;
import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.BlockEntityProgress;
import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.entity.slot.FaktocraftSlot;
import com.faktocraft.common.entity.slot.SlotBattery;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.enums.ReplicatorMode;
import com.faktocraft.common.enums.UpgradeType;
import com.faktocraft.common.fluid.ModFluids;
import com.faktocraft.common.interfaces.entity.IElectricSlot;
import com.faktocraft.common.interfaces.entity.IMachineActions;
import com.faktocraft.common.interfaces.entity.ISupportUpgrades;
import com.faktocraft.common.interfaces.entity.ITileSound;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketReplicatorAction;
import com.faktocraft.common.registries.machines.M4Registry;
import com.faktocraft.common.registries.ModComponentsFluids;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.ModSounds;
import com.faktocraft.common.util.StackHandlerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
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

public class BlockEntityReplicator extends FaktocraftBlockEntity
    implements IEnergyBlock, ISupportUpgrades, ITileSound, IMachineActions.IReplicatorActions {

  public static final int MEMORY_SLOT = 0;
  public static final int OUTPUT_SLOT = 1;

  public final BlockEntityProgress progress = new BlockEntityProgress();
  protected ReplicatorMode mode = ReplicatorMode.WAITING;
  public final FluidStorage matterTank = new FluidStorage(
      ModConfig.server().replicator_matter_capacity,
      fluidStack -> fluidStack.getFluid() == ModFluids.MATTER.still());

  private final LazyOptional<IFluidHandler> fluidHandlerCap = LazyOptional.of(() -> matterTank);

  protected ScannerResult result = ScannerResult.EMPTY;

  private ItemStack cachedMemoryStack = ItemStack.EMPTY;
  private ScannerResult cachedCardResult = ScannerResult.EMPTY;

  public BlockEntityReplicator(BlockPos pos, BlockState state) {
    super(M4Registry.REPLICATOR_BE, pos, state);
    createEnergyStorage(0, ModConfig.server().replicator_energy_capacity, EnergyType.RECEIVE, EnergyTier.ULTRA);
    initBatterySlots();
    matterTank.setChangeListener(this::setChanged);
  }

  @Override
  public void tickWork(BlockState state) {
    boolean active = false;

    ItemStack memoryStack = getItemStackHandler().getStackInSlot(MEMORY_SLOT);
    if (!ItemStack.matches(cachedMemoryStack, memoryStack)) {
      cachedMemoryStack = memoryStack.copy();
      cachedCardResult = readCardPattern(memoryStack);
    }
    ScannerResult cardResult = cachedCardResult;
    if (!cardResult.equals(result)) {
      result = cardResult;
      progress.setBoth(-1);
      mode = ReplicatorMode.WAITING;
      shouldUpdateState = true;
    }

    if (!result.isEmpty() && getEnergyStorage().energyStored() > 0
        && (mode.getId() == 1 || mode.getId() == 2)) {
      int matterCost = result.getMatterCost();
      int energyCost = (int) (result.getEnergyCost() * getEnergyUsageFactor());
      int duration = Math.max(matterCost, 1);

      if (progress.getProgress() == -1) {
        progress.setData(0, duration);
      }

      progress.rescaleMax(getSpeedFactor() * duration);

      if (canWork()) {

        int totalTicks = Math.max(1, (int) Math.ceil(progress.getProgressMax()));
        int ticksDone = Math.min(Math.max((int) progress.getProgress(), 0), totalTicks - 1);
        int matterCostTick = (int) ((long) matterCost * (ticksDone + 1) / totalTicks
            - (long) matterCost * ticksDone / totalTicks);
        if (matterTank.getFluidAmount() >= matterCostTick && getEnergyStorage().energyStored() > 0) {
          if ((result.getEnergyCost() == 0 || getEnergyStorage().consumeEnergy(energyCost, true) == energyCost)
              && progress.getProgress() <= progress.getProgressMax()) {
            matterTank.drain(matterCostTick, IFluidHandler.FluidAction.EXECUTE);
            active = true;
            progress.incProgress(1);
            getEnergyStorage().consumeEnergy(energyCost, false);
            getEnergyStorage().updateConsumed(energyCost);
          }

          if (progress.getProgress() >= progress.getProgressMax()) {
            StackHandlerHelper.incMachineOutputStack(getItemStackHandler(), OUTPUT_SLOT,
                result.getResultStack().copy());
            progress.setBoth(-1);

            if (mode == ReplicatorMode.SINGLE_RUN) {
              mode = ReplicatorMode.WAITING;
            }
          }
        }
      }
    }

    if (progress.changed()) {
      progress.clearChanged();
      shouldUpdateState = true;
    }

    setActive(active);
  }

  @Override
  public ArrayList<FaktocraftSlot> addInventorySlot(ArrayList<FaktocraftSlot> slots) {
    slots.add(new FaktocraftSlot(MEMORY_SLOT, 68, 24, InventorySlotType.INPUT, GuiSlotType.NORMAL_BLANK, 67, 23));
    slots.add(new FaktocraftSlot(OUTPUT_SLOT, 120, 23, InventorySlotType.OUTPUT, GuiSlotType.LARGE, 115, 18));
    return super.addInventorySlot(slots);
  }

  @Override
  public ArrayList<IElectricSlot> addBatterySlot(ArrayList<IElectricSlot> slots) {
    slots.add(new SlotBattery(0, 152, 62, false));
    return super.addBatterySlot(slots);
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable Direction side) {
    return true;
  }

  @Override
  public int customEnergyReceiveTick() {
    return 12288;
  }

  @Override
  public SoundEvent getSoundEvent() {
    return ModSounds.REPLICATOR;
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    if (slot == MEMORY_SLOT) {
      if (stack.getItem() != ModItems.MEMORY_CARD) {
        return false;
      }
      ScannerResult cardResult = ModComponentsFluids.getScannerResult(stack);
      return cardResult != null && !cardResult.isEmpty();
    }
    return false;
  }

  @Override
  public int getCustomSlotLimit(int slot) {
    return slot == MEMORY_SLOT ? 1 : super.getCustomSlotLimit(slot);
  }

  private static ScannerResult readCardPattern(ItemStack memoryStack) {
    if (memoryStack.getItem() == ModItems.MEMORY_CARD) {
      ScannerResult cardResult = ModComponentsFluids.getScannerResult(memoryStack);
      if (cardResult != null && !cardResult.isEmpty()) {
        return cardResult;
      }
    }
    return ScannerResult.EMPTY;
  }

  @Override
  public List<UpgradeType> getSupportedUpgrades() {
    return List.of(UpgradeType.OVERCLOCKER, UpgradeType.EFFICIENCY);
  }

  public Runnable clientClickStop() {
    BlockPos pos = getBlockPos();
    return () -> ModNetworking.sendToServer(new PacketReplicatorAction(pos, 0));
  }

  public Runnable clientClickSingleRun() {
    BlockPos pos = getBlockPos();
    return () -> ModNetworking.sendToServer(new PacketReplicatorAction(pos, 1));
  }

  public Runnable clientClickRepeatRun() {
    BlockPos pos = getBlockPos();
    return () -> ModNetworking.sendToServer(new PacketReplicatorAction(pos, 2));
  }

  @Override
  public void stopRun() {
    mode = ReplicatorMode.WAITING;
    updateBlockState();
  }

  @Override
  public void singleRun() {
    if (!result.isEmpty() && canWork()) {
      mode = ReplicatorMode.SINGLE_RUN;
      updateBlockState();
    }
  }

  @Override
  public void repeatRun() {
    if (!result.isEmpty() && canWork()) {
      mode = ReplicatorMode.REPEAT_RUN;
      updateBlockState();
    }
  }

  public ReplicatorMode getMode() {
    return mode;
  }

  private boolean canWork() {
    ItemStack outputStack = getItemStackHandler().getStackInSlot(OUTPUT_SLOT);
    return outputStack.isEmpty()
        || (outputStack.getCount() < outputStack.getMaxStackSize()
            && outputStack.getItem() == result.getResultStack().getItem());
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

    CompoundTag fluidTag = new CompoundTag();
    matterTank.save(fluidTag);
    tag.put("fluidMatterStorage", fluidTag);

    CompoundTag resultTag = new CompoundTag();
    result.save(resultTag);
    tag.put("result", resultTag);

    tag.putInt("mode", mode.getId());
    super.saveAdditional(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    if (tag.contains("progress")) {
      progress.load(tag.getCompound("progress"));
    }
    if (tag.contains("fluidMatterStorage")) {
      matterTank.load(tag.getCompound("fluidMatterStorage"));
    }

    this.result = tag.contains("result") ? ScannerResult.load(tag.getCompound("result")) : ScannerResult.EMPTY;
    this.mode = ReplicatorMode
        .getModeFromId(tag.contains("mode") ? tag.getInt("mode") : ReplicatorMode.WAITING.getId());
  }

  @Override
  public java.util.List<com.faktocraft.common.entity.block.FluidStorage> getGuiTanks() {
    return java.util.List.of(matterTank);
  }
}
