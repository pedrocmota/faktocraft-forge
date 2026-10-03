package com.faktocraft.common.block.impl.machines.fueling_station;

import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.entity.slot.FaktocraftSlot;
import com.faktocraft.common.entity.slot.SlotBattery;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.interfaces.entity.IElectricSlot;
import com.faktocraft.common.util.EnergyCosts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import com.faktocraft.common.util.transfer.Capability;
import com.faktocraft.common.util.transfer.ForgeCapabilities;
import com.faktocraft.common.util.transfer.InvWrapper;
import com.faktocraft.common.util.transfer.LazyOptional;
import net.neoforged.neoforge.fluids.FluidStack;
import com.faktocraft.common.util.transfer.IFluidHandler;
import com.faktocraft.common.util.transfer.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;

public class BlockEntityFuelingStation extends FaktocraftBlockEntity implements IEnergyBlock {

  public static final int ITEM_SLOT = 0;

  public static final int TANK_CAPACITY = 16000;
  public static final int TRANSFER_MB_PER_TICK = 50;
  public static final int POWER_PER_TICK = 8;

  public final FluidStorage tank = new FluidStorage(TANK_CAPACITY);
  private boolean tanksDirty = false;

  private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(TankHandler::new);

  public BlockEntityFuelingStation(BlockPos pos, BlockState state) {
    super(FuelingStationRegistry.FUELING_STATION_BLOCK_ENTITY, pos, state);
    createEnergyStorage(0, 8000, EnergyType.RECEIVE, EnergyTier.LOW);
    initBatterySlots();
    tank.setChangeListener(() -> {
      setChanged();
      tanksDirty = true;
    });
  }

  @Override
  public ArrayList<FaktocraftSlot> addInventorySlot(ArrayList<FaktocraftSlot> slots) {
    slots.add(new FaktocraftSlot(ITEM_SLOT, 81, 47, InventorySlotType.INPUT, GuiSlotType.NORMAL, 80, 46));
    return super.addInventorySlot(slots);
  }

  @Override
  public ArrayList<IElectricSlot> addBatterySlot(ArrayList<IElectricSlot> slots) {
    slots.add(new SlotBattery(0, 152, 62, false));
    return super.addBatterySlot(slots);
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    return slot == ITEM_SLOT && com.faktocraft.common.util.transfer.CapabilityBridge.fluidHandlerItem(stack) != null;
  }

  @Override
  public int getCustomSlotLimit(int slot) {
    return slot == ITEM_SLOT ? 1 : super.getCustomSlotLimit(slot);
  }

  private LazyOptional<com.faktocraft.common.util.transfer.IItemHandler> finishedCellCap = LazyOptional.empty();

  private boolean isFinished(ItemStack stack) {
    if (stack.isEmpty()) {
      return false;
    }
    if (tank.getFluidAmount() <= 0) {
      return true;
    }
    IFluidHandlerItem handler = com.faktocraft.common.util.transfer.LazyOptional
        .of(() -> com.faktocraft.common.util.transfer.CapabilityBridge.fluidHandlerItem(stack))
        .resolve().orElse(null);
    if (handler == null) {
      return true;
    }
    FluidStack offer = new FluidStack(tank.getFluidStack().getFluid(),
        Math.min(TRANSFER_MB_PER_TICK, tank.getFluidAmount()));
    return handler.fill(offer, IFluidHandler.FluidAction.SIMULATE) <= 0;
  }

  @Override
  public void tickWork(BlockState state) {
    if (level == null || level.isClientSide()) {
      return;
    }
    getEnergyStorage().updateConsumed(0);
    boolean active = false;
    ItemStack target = getItemStackHandler().getStackInSlot(ITEM_SLOT);
    if (!target.isEmpty() && tank.getFluidAmount() > 0) {
      IFluidHandlerItem handler = com.faktocraft.common.util.transfer.LazyOptional
          .of(() -> com.faktocraft.common.util.transfer.CapabilityBridge.fluidHandlerItem(target))
          .resolve().orElse(null);
      if (handler != null) {
        FluidStack offer = new FluidStack(tank.getFluidStack().getFluid(),
            Math.min(TRANSFER_MB_PER_TICK, tank.getFluidAmount()));
        int accepted = handler.fill(offer.copy(), IFluidHandler.FluidAction.SIMULATE);
        int energyCost = EnergyCosts.perTick(POWER_PER_TICK, getEnergyUsageFactor());
        if (accepted > 0 && getEnergyStorage().consumeEnergy(energyCost, true) == energyCost) {
          getEnergyStorage().consumeEnergy(energyCost, false);
          getEnergyStorage().updateConsumed(energyCost);
          handler.fill(new FluidStack(offer.getFluid(), accepted), IFluidHandler.FluidAction.EXECUTE);
          tank.takeFluid(accepted, false);
          getItemStackHandler().setStackInSlot(ITEM_SLOT, handler.getContainer());
          active = true;
        }
      }
    }
    setActive(active);
    if (tanksDirty) {
      tanksDirty = false;
      updateBlockState();
    }
    updateGaugeLevel();
  }

  private void updateGaugeLevel() {
    int lvl = tank.getFluidAmount() <= 0 ? 0
        : Math.max(1, Math.min(4, (int) Math.ceil(tank.getFluidAmount() * 4.0 / tank.getCapacityMb())));
    BlockState current = level.getBlockState(getBlockPos());
    if (current.hasProperty(BlockFuelingStation.LEVEL)
        && current.getValue(BlockFuelingStation.LEVEL) != lvl) {
      level.setBlockAndUpdate(getBlockPos(), current.setValue(BlockFuelingStation.LEVEL, lvl));
    }
  }

  private class TankHandler implements IFluidHandler {
    @Override
    public int getTanks() {
      return 1;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int index) {
      return tank.getFluidStack();
    }

    @Override
    public int getTankCapacity(int index) {
      return tank.getCapacityMb();
    }

    @Override
    public boolean isFluidValid(int index, @NotNull FluidStack stack) {
      return tank.getFluidAmount() == 0 || stack.getFluid().isSame(tank.getFluidStack().getFluid());
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
      if (resource.isEmpty() || !isFluidValid(0, resource)) {
        return 0;
      }
      return tank.fillFluid(resource, resource.getAmount(), action.simulate());
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
      if (resource.isEmpty() || tank.getFluidAmount() == 0
          || !resource.getFluid().isSame(tank.getFluidStack().getFluid())) {
        return FluidStack.EMPTY;
      }
      return drain(resource.getAmount(), action);
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
      int amount = Math.min(maxDrain, tank.getFluidAmount());
      if (amount <= 0) {
        return FluidStack.EMPTY;
      }
      FluidStack drained = new FluidStack(tank.getFluidStack().getFluid(), amount);
      tank.takeFluid(amount, action.simulate());
      return drained;
    }
  }

  @NotNull
  @Override
  public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.FLUID_HANDLER) {
      if (isFrontSide(side)) {
        return LazyOptional.empty();
      }
      return fluidCap.cast();
    }
    if (cap == ForgeCapabilities.ITEM_HANDLER && hasInventory() && getItemStackHandler() != null) {
      if (isFrontSide(side)) {
        return LazyOptional.empty();
      }
      if (!finishedCellCap.isPresent()) {
        finishedCellCap = LazyOptional.of(() -> new InvWrapper(getItemStackHandler()) {
          @NotNull
          @Override
          public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == ITEM_SLOT && isFinished(getStackInSlot(slot))
                ? super.extractItem(slot, amount, simulate)
                : ItemStack.EMPTY;
          }

          @Override
          public int getSlotLimit(int slot) {
            return getCustomSlotLimit(slot);
          }
        });
      }
      return finishedCellCap.cast();
    }
    return super.getCapability(cap, side);
  }

  @Override
  public void setRemoved() {
    super.setRemoved();
    fluidCap.invalidate();
    finishedCellCap.invalidate();
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    tag.putBoolean("active", activeState);
    CompoundTag tankTag = new CompoundTag();
    tank.save(tankTag);
    tag.put("tank", tankTag);
    super.saveAdditional(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    activeState = tag.getBooleanOr("active", false);
    if (tag.contains("tank")) {
      tank.load(tag.getCompoundOrEmpty("tank"));
    }
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable Direction side) {
    return true;
  }

  @Override
  public java.util.List<FluidStorage> getGuiTanks() {
    return java.util.List.of(tank);
  }

  @Override
  protected boolean syncEnergyToWorld() {
    return true;
  }
}
