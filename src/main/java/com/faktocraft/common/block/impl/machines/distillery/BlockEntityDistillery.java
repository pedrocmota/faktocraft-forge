package com.faktocraft.common.block.impl.machines.distillery;

import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.entity.slot.FaktocraftSlot;
import com.faktocraft.common.entity.slot.SlotBattery;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.fluid.ModFluids;
import com.faktocraft.common.interfaces.entity.IElectricSlot;
import com.faktocraft.common.util.EnergyCosts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;

public class BlockEntityDistillery extends FaktocraftBlockEntity
    implements IEnergyBlock, com.faktocraft.common.interfaces.entity.ISupportUpgrades,
    com.faktocraft.common.interfaces.entity.ITileSound {

  public static final int SULFUR_SLOT = 0;

  public static final int OIL_PER_OP = 100;
  public static final int ACID_PER_OP = 20;
  public static final int WATER_PER_OP = 40;
  public static final int FUEL_PER_OP = 80;
  public static final int DURATION_TICKS = 100;
  public static final int POWER_PER_TICK = 16;
  public static final float SULFUR_CHANCE = 0.05F;

  public final FluidStorage oilTank = new FluidStorage(4000);
  public final FluidStorage acidTank = new FluidStorage(2000);
  public final FluidStorage waterTank = new FluidStorage(4000);
  public final FluidStorage fuelTank = new FluidStorage(4000, v -> v.getFluid() == ModFluids.FUEL.still())
      .markOutputOnly();

  public final com.faktocraft.common.entity.block.BlockEntityProgress progress = new com.faktocraft.common.entity.block.BlockEntityProgress(
      0, DURATION_TICKS);
  private int healTimer = 0;
  private boolean tanksDirty = false;

  private final LazyOptional<IFluidHandler> fluidCap = LazyOptional.of(RoutingHandler::new);

  public BlockEntityDistillery(BlockPos pos, BlockState state) {
    super(DistilleryRegistry.DISTILLERY_BLOCK_ENTITY, pos, state);
    createEnergyStorage(0, com.faktocraft.common.config.ModConfig.server().distillery_energy_capacity,
        EnergyType.RECEIVE, EnergyTier.MEDIUM);
    initBatterySlots();
    Runnable onTankChange = () -> {
      setChanged();
      tanksDirty = true;
    };
    oilTank.setChangeListener(onTankChange);
    acidTank.setChangeListener(onTankChange);
    waterTank.setChangeListener(onTankChange);
    fuelTank.setChangeListener(onTankChange);
  }

  @Override
  public ArrayList<FaktocraftSlot> addInventorySlot(ArrayList<FaktocraftSlot> slots) {
    slots.add(new FaktocraftSlot(SULFUR_SLOT, 95, 69, InventorySlotType.OUTPUT, GuiSlotType.NORMAL, 94, 68));
    return super.addInventorySlot(slots);
  }

  @Override
  public ArrayList<IElectricSlot> addBatterySlot(ArrayList<IElectricSlot> slots) {
    slots.add(new SlotBattery(0, 152, 62, false));
    return super.addBatterySlot(slots);
  }

  private boolean canRun() {
    if (oilTank.getFluidAmount() < OIL_PER_OP || acidTank.getFluidAmount() < ACID_PER_OP
        || waterTank.getFluidAmount() < WATER_PER_OP) {
      return false;
    }
    if (fuelTank.getCapacityMb() - fuelTank.getFluidAmount() < FUEL_PER_OP) {
      return false;
    }
    ItemStack out = getItemStackHandler().getStackInSlot(SULFUR_SLOT);
    return out.isEmpty() || (out.is(com.faktocraft.common.registries.ModItems.SULFUR_DUST)
        && out.getCount() < out.getMaxStackSize());
  }

  @Override
  public void tickWork(BlockState state) {
    if (level == null || level.isClientSide()) {
      return;
    }
    if (++healTimer >= 200) {
      healTimer = 0;
      BlockDistillery.healStructure(level, getBlockPos());
    }
    getEnergyStorage().updateConsumed(0);
    boolean active = false;
    boolean updateState = false;
    progress.rescaleMax(getSpeedFactor() * DURATION_TICKS);
    int energyCost = EnergyCosts.perTick(POWER_PER_TICK, getEnergyUsageFactor());
    if (canRun()) {
      if (getEnergyStorage().consumeEnergy(energyCost, true) == energyCost) {
        getEnergyStorage().consumeEnergy(energyCost, false);
        getEnergyStorage().updateConsumed(energyCost);
        progress.incProgress(1);
        active = true;
        if (progress.getProgress() >= progress.getProgressMax()) {
          progress.setProgress(0);
          oilTank.takeFluid(OIL_PER_OP, false);
          acidTank.takeFluid(ACID_PER_OP, false);
          waterTank.takeFluid(WATER_PER_OP, false);
          fuelTank.fillFluid(new FluidStack(ModFluids.FUEL.still(), FUEL_PER_OP), FUEL_PER_OP, false);
          if (level.getRandom().nextFloat() < SULFUR_CHANCE) {
            ItemStack out = getItemStackHandler().getStackInSlot(SULFUR_SLOT);
            if (out.isEmpty()) {
              getItemStackHandler().setStackInSlot(SULFUR_SLOT,
                  new ItemStack(com.faktocraft.common.registries.ModItems.SULFUR_DUST));
            } else {
              out.grow(1);
            }
          }
        }
      }
    } else if (progress.getProgress() > 0) {
      progress.setProgress(0);
    }
    if (progress.changed()) {
      progress.clearChanged();
      updateState = true;
    }
    if (tanksDirty) {
      tanksDirty = false;
      updateState = true;
    }
    setActive(active);
    if (updateState) {
      updateBlockState();
    }
  }

  @Override
  public void tickClient(BlockState state) {
    super.tickClient(state);
    emitChimneyFlame(state);
  }

  private void emitChimneyFlame(BlockState state) {
    net.minecraft.world.level.Level level = getLevel();
    if (level == null || !BlockDistillery.isLit(state)) {
      return;
    }
    net.minecraft.util.RandomSource random = level.getRandom();
    BlockPos pos = getBlockPos();
    double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
    double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.2;

    double top = pos.getY() + BlockDistillery.TOWER_HEIGHT + 1.98;
    level.addParticle(net.minecraft.core.particles.ParticleTypes.FLAME, x, top, z, 0.0, 0.01, 0.0);
    if (random.nextInt(3) == 0) {
      level.addParticle(net.minecraft.core.particles.ParticleTypes.SMALL_FLAME,
          x, top + 0.12, z, 0.0, 0.014, 0.0);
    }
    if (random.nextInt(4) == 0) {
      level.addParticle(net.minecraft.core.particles.ParticleTypes.CAMPFIRE_COSY_SMOKE, x, top + 0.3, z,
          (random.nextDouble() - 0.5) * 0.01, 0.06, (random.nextDouble() - 0.5) * 0.01);
    }
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    return false;
  }

  private class RoutingHandler implements IFluidHandler {
    @Override
    public int getTanks() {
      return 4;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
      return tankByIndex(tank).getFluidStack();
    }

    @Override
    public int getTankCapacity(int tank) {
      return tankByIndex(tank).getCapacityMb();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
      return routeFor(stack) == tankByIndex(tank);
    }

    private FluidStorage tankByIndex(int tank) {
      return switch (tank) {
        case 0 -> oilTank;
        case 1 -> acidTank;
        case 2 -> waterTank;
        default -> fuelTank;
      };
    }

    @Nullable
    private FluidStorage routeFor(FluidStack stack) {
      if (stack.getFluid().isSame(ModFluids.OIL.still())) {
        return oilTank;
      }
      if (stack.getFluid().isSame(ModFluids.SULFURIC_ACID.still())) {
        return acidTank;
      }
      if (stack.getFluid().isSame(Fluids.WATER)) {
        return waterTank;
      }
      return null;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
      FluidStorage target = routeFor(resource);
      if (target == null) {
        return 0;
      }
      return target.fillFluid(resource, resource.getAmount(), action.simulate());
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
      if (!resource.getFluid().isSame(ModFluids.FUEL.still())) {
        return FluidStack.EMPTY;
      }
      return drain(resource.getAmount(), action);
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
      int amount = Math.min(maxDrain, fuelTank.getFluidAmount());
      if (amount <= 0) {
        return FluidStack.EMPTY;
      }
      FluidStack drained = new FluidStack(ModFluids.FUEL.still(), amount);
      fuelTank.takeFluid(amount, action.simulate());
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
    return super.getCapability(cap, side);
  }

  @Override
  public void setRemoved() {
    super.setRemoved();
    fluidCap.invalidate();
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    tag.putBoolean("active", activeState);
    CompoundTag progressTag = new CompoundTag();
    progress.save(progressTag);
    tag.put("op", progressTag);
    for (var entry : new Object[][] { { "oil", oilTank }, { "acid", acidTank },
        { "water", waterTank }, { "fuel", fuelTank } }) {
      CompoundTag t = new CompoundTag();
      ((FluidStorage) entry[1]).save(t);
      tag.put((String) entry[0], t);
    }
    super.saveAdditional(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    activeState = tag.getBoolean("active");
    if (tag.contains("op")) {
      progress.load(tag.getCompound("op"));
    }
    for (var entry : new Object[][] { { "oil", oilTank }, { "acid", acidTank },
        { "water", waterTank }, { "fuel", fuelTank } }) {
      if (tag.contains((String) entry[0])) {
        ((FluidStorage) entry[1]).load(tag.getCompound((String) entry[0]));
      }
    }
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable Direction side) {
    return true;
  }

  @Override
  public java.util.List<com.faktocraft.common.enums.UpgradeType> getSupportedUpgrades() {
    return java.util.List.of(com.faktocraft.common.enums.UpgradeType.OVERCLOCKER,
        com.faktocraft.common.enums.UpgradeType.EFFICIENCY);
  }

  @Nullable
  @Override
  public net.minecraft.sounds.SoundEvent getSoundEvent() {
    return com.faktocraft.common.registries.ModSounds.DISTILLERY;
  }

  @Override
  public net.minecraft.world.phys.AABB getRenderBoundingBox() {
    return new net.minecraft.world.phys.AABB(getBlockPos()).expandTowards(0, 5, 0);
  }

  @Override
  public java.util.List<com.faktocraft.common.entity.block.FluidStorage> getGuiTanks() {
    return java.util.List.of(oilTank, acidTank, waterTank, fuelTank);
  }
}
