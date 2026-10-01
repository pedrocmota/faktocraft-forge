package com.faktocraft.common.block.impl.machines.nuclear_reactor;

import com.faktocraft.common.util.PlayerMessages;
import com.faktocraft.Faktocraft;
import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.config.ServerConfig;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.entity.block.FluidStorage;
import com.faktocraft.common.entity.slot.FaktocraftSlot;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.GuiSlotType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.interfaces.entity.ITileSound;
import com.faktocraft.common.item.base.FluidItem;
import com.faktocraft.common.item.impl.reactor.CoolantCell;
import com.faktocraft.common.radiation.RadiationSources;
import com.faktocraft.common.registries.ModItems;
import com.faktocraft.common.registries.ModSounds;
import com.faktocraft.common.util.ItemStackHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import com.faktocraft.common.util.transfer.Capability;
import com.faktocraft.common.util.transfer.ForgeCapabilities;
import com.faktocraft.common.util.transfer.LazyOptional;
import net.neoforged.neoforge.fluids.FluidStack;
import com.faktocraft.common.util.transfer.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockEntityNuclearReactor extends FaktocraftBlockEntity implements IEnergyBlock, ITileSound {

  public static final int GRID_WIDTH = 4;
  public static final int GRID_HEIGHT = 3;
  public static final int SLOTS = GRID_WIDTH * GRID_HEIGHT;
  public static final int ROD_DAMAGE_STEPS = 2400;
  public static final int STATUS_IDLE = 0;
  public static final int STATUS_RUNNING = 1;
  public static final int STATUS_FULL = 2;
  public static final int STATUS_REDSTONE = 3;
  public static final int STATUS_HOT = 4;
  public static final int STATUS_CRITICAL = 5;
  public static final float HOT_RATIO = 0.5F;
  public static final float CRITICAL_RATIO = 0.8F;
  public static final float IDLE_RADIATION = 0.02F;
  private static final float STANDBY_RADIATION = 0.05F;
  private static final float RUNNING_RADIATION = 0.15F;
  private static final int SYNC_INTERVAL = 10;
  private static final int SLOW_DIVISOR = 2;
  private static final int ALARM_INTERVAL = 40;
  private static final int MELTDOWN_MESSAGE_RANGE = 128;
  private static final int GRID_X = 32;
  private static final int GRID_Y = 17;
  private static final int KIND_NONE = 0;
  private static final int KIND_ROD = 1;
  private static final int KIND_REFLECTOR = 2;
  private static final int KIND_MEDIUM_CELL = 3;
  private static final int KIND_LARGE_CELL = 4;

  public final FluidStorage water = new FluidStorage(ModConfig.server().reactor_water_capacity,
      stack -> stack.getFluid().isSame(Fluids.WATER));
  private final LazyOptional<IFluidHandler> waterCap = LazyOptional.of(() -> water);
  private final float[] cellCarry = new float[SLOTS];
  private final int[] kinds = new int[SLOTS];

  private int heat;
  private boolean running;
  private boolean throttled;
  private int reactionOutput;
  private int outputPerTick;
  private int heatPerTick;
  private int coolingPerTick;
  private int rods;
  private int status = STATUS_IDLE;
  private int lastSyncedHeat = -1;
  private int lastSyncedStatus = -1;
  private int lastSyncedWater = -1;
  private int burnTick;
  private int ticks;
  private float coolCarry;
  private float waterCarry;

  public BlockEntityNuclearReactor(BlockPos pos, BlockState state) {
    super(NuclearReactorRegistry.NUCLEAR_REACTOR_BLOCK_ENTITY, pos, state);
    createEnergyStorage(0, ModConfig.server().reactor_energy_capacity, EnergyType.EXTRACT, EnergyTier.ULTRA);
    water.setChangeListener(this::setChanged);
  }

  @Override
  public ArrayList<FaktocraftSlot> addInventorySlot(ArrayList<FaktocraftSlot> slots) {
    for (int i = 0; i < SLOTS; i++) {
      int x = GRID_X + (i % GRID_WIDTH) * 18;
      int y = GRID_Y + (i / GRID_WIDTH) * 18;
      slots.add(new FaktocraftSlot(i, x + 1, y + 1, InventorySlotType.NORMAL, GuiSlotType.NORMAL, x, y));
    }
    return slots;
  }

  @Override
  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    return kindOf(stack) != KIND_NONE;
  }

  @Override
  public int getCustomSlotLimit(int slot) {
    return 1;
  }

  public static boolean isComponent(ItemStack stack) {
    return kindOf(stack) != KIND_NONE;
  }

  private static int kindOf(ItemStack stack) {
    if (stack.isEmpty()) {
      return KIND_NONE;
    }
    if (stack.is(ModItems.FUEL_ROD)) {
      return KIND_ROD;
    }
    if (stack.is(ModItems.NEUTRON_REFLECTOR)) {
      return KIND_REFLECTOR;
    }
    if (stack.is(ModItems.MEDIUM_COOLANT_CELL)) {
      return KIND_MEDIUM_CELL;
    }
    if (stack.is(ModItems.LARGE_COOLANT_CELL)) {
      return KIND_LARGE_CELL;
    }
    return KIND_NONE;
  }

  public static String statusKey(int status) {
    String name = switch (status) {
      case STATUS_RUNNING -> "running";
      case STATUS_FULL -> "full";
      case STATUS_REDSTONE -> "redstone";
      case STATUS_HOT -> "hot";
      case STATUS_CRITICAL -> "critical";
      default -> "idle";
    };
    return "gui." + Faktocraft.MODID + ".reactor.status_" + name;
  }

  public BlockPos origin() {
    ReactorPart core = BlockNuclearReactor.CORE_PART;
    return worldPosition.offset(-core.x(), -core.y(), -core.z());
  }

  public BlockPos center() {
    return origin().offset(ReactorPart.SIZE / 2, ReactorPart.SIZE / 2, ReactorPart.SIZE / 2);
  }

  @Override
  public void tickWork(BlockState state) {
    if (!(level instanceof ServerLevel serverLevel)) {
      return;
    }
    ServerConfig config = ModConfig.server();
    ticks++;
    computeGrid(config);
    boolean stopped = redstoneStopped();
    boolean fuelled = rods > 0 && !stopped;
    boolean full = fuelled && getEnergyStorage().generateEnergy(1, true) < 1;
    int produced = 0;
    if (fuelled && !full) {
      int wanted = Math.round(reactionOutput * (1.0F + heatRatio() * (float) config.reactor_heat_boost));
      produced = wanted > 0 ? getEnergyStorage().generateEnergy(wanted, true) : 0;
      if (produced > 0) {
        getEnergyStorage().generateEnergy(produced, false);
      }
    }
    if (fuelled && (!full || ticks % SLOW_DIVISOR == 0)) {
      heat += heatPerTick;
      burnRods(config);
    }
    getEnergyStorage().updateGenerated(produced);
    outputPerTick = produced;
    cool(config);
    running = fuelled;
    throttled = full;
    float ratio = heatRatio();
    if (ratio >= CRITICAL_RATIO) {
      status = STATUS_CRITICAL;
    } else if (ratio >= HOT_RATIO) {
      status = STATUS_HOT;
    } else if (rods == 0) {
      status = STATUS_IDLE;
    } else if (stopped) {
      status = STATUS_REDSTONE;
    } else if (full) {
      status = STATUS_FULL;
    } else {
      status = STATUS_RUNNING;
    }
    if (heat >= getMaxHeat()) {
      meltdown(serverLevel, config);
      return;
    }
    if (ratio >= CRITICAL_RATIO && ticks % ALARM_INTERVAL == 0) {
      BlockPos center = center();
      serverLevel.playSound(null, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
          ModSounds.REACTOR_ALARM, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
    boolean changed = heat != lastSyncedHeat || status != lastSyncedStatus
        || water.getFluidAmount() != lastSyncedWater;
    if (ticks % SYNC_INTERVAL == 0 && changed) {
      lastSyncedHeat = heat;
      lastSyncedStatus = status;
      lastSyncedWater = water.getFluidAmount();
      updateBlockState();
    } else if (fuelled || heat > 0) {
      setChanged();
    }
  }

  private void computeGrid(ServerConfig config) {
    ItemStackHandler handler = getItemStackHandler();
    for (int i = 0; i < SLOTS; i++) {
      kinds[i] = kindOf(handler.getStackInSlot(i));
    }
    int output = 0;
    int heating = 0;
    int count = 0;
    for (int i = 0; i < SLOTS; i++) {
      if (kinds[i] != KIND_ROD) {
        continue;
      }
      count++;
      int rodNeighbours = 0;
      int reflectors = 0;
      for (int n : neighbours(i)) {
        if (kinds[n] == KIND_ROD) {
          rodNeighbours++;
        } else if (kinds[n] == KIND_REFLECTOR) {
          reflectors++;
        }
      }
      output += config.reactor_rod_energy + config.reactor_rod_bonus * (rodNeighbours + reflectors);
      heating += config.reactor_rod_heat + config.reactor_rod_heat_bonus * rodNeighbours
          + config.reactor_reflector_heat * reflectors;
    }
    reactionOutput = output;
    heatPerTick = heating;
    rods = count;
  }

  private static List<Integer> neighbours(int slot) {
    int x = slot % GRID_WIDTH;
    int z = slot / GRID_WIDTH;
    List<Integer> out = new ArrayList<>(4);
    if (x > 0) {
      out.add(slot - 1);
    }
    if (x < GRID_WIDTH - 1) {
      out.add(slot + 1);
    }
    if (z > 0) {
      out.add(slot - GRID_WIDTH);
    }
    if (z < GRID_HEIGHT - 1) {
      out.add(slot + GRID_WIDTH);
    }
    return out;
  }

  private void cool(ServerConfig config) {
    ItemStackHandler handler = getItemStackHandler();
    int capacity = config.reactor_passive_cooling;
    for (int i = 0; i < SLOTS; i++) {
      int kind = kinds[i];
      if (kind != KIND_MEDIUM_CELL && kind != KIND_LARGE_CELL) {
        cellCarry[i] = 0.0F;
        continue;
      }
      ItemStack cell = handler.getStackInSlot(i);
      int coolant = FluidItem.getFluidAmount(cell);
      if (coolant <= 0 || !CoolantCell.isCoolant(new FluidStack(FluidItem.getFluid(cell), coolant))) {
        cellCarry[i] = 0.0F;
        continue;
      }
      capacity += kind == KIND_LARGE_CELL ? config.reactor_cell_cooling * 2 : config.reactor_cell_cooling;
      if (heat <= 0) {
        continue;
      }
      int cellCapacity = cell.getItem() instanceof CoolantCell coolantCell ? coolantCell.getFluidCapacity() : coolant;
      cellCarry[i] += cellCapacity / (float) Math.max(1, config.reactor_cell_life_ticks);
      int used = Math.min((int) cellCarry[i], coolant);
      if (used > 0) {
        cellCarry[i] -= used;
        FluidItem.setFluid(cell, FluidItem.getFluid(cell), coolant - used);
        handler.setStackInSlot(i, cell);
      }
    }
    if (heat <= 0) {
      coolingPerTick = 0;
      coolCarry = 0.0F;
      waterCarry = 0.0F;
      return;
    }
    float ratio = heatRatio();
    if (!water.isEmpty()) {
      int perMb = Math.max(1, config.reactor_bucket_cooling / 1000);
      capacity += config.reactor_water_per_tick * perMb;
      waterCarry += config.reactor_water_per_tick * ratio;
      int mb = Math.min((int) waterCarry, water.getFluidAmount());
      if (mb > 0) {
        water.takeFluid(mb, false);
        waterCarry -= mb;
      }
    }
    float cooling = Math.max(1.0F, capacity * ratio);
    coolingPerTick = Math.round(cooling);
    coolCarry += cooling;
    int removed = (int) coolCarry;
    coolCarry -= removed;
    heat = Math.max(0, heat - removed);
  }

  private void burnRods(ServerConfig config) {
    int ticksPerStep = Math.max(1, config.reactor_rod_life_ticks / ROD_DAMAGE_STEPS);
    if (++burnTick < ticksPerStep) {
      return;
    }
    burnTick = 0;
    ItemStackHandler handler = getItemStackHandler();
    for (int i = 0; i < SLOTS; i++) {
      if (kinds[i] != KIND_ROD) {
        continue;
      }
      ItemStack rod = handler.getStackInSlot(i);
      int damage = rod.getDamageValue() + 1;
      if (damage >= rod.getMaxDamage()) {
        handler.setStackInSlot(i, new ItemStack(ModItems.DEPLETED_FUEL_ROD));
      } else {
        rod.setDamageValue(damage);
        handler.setStackInSlot(i, rod);
      }
    }
  }

  private void meltdown(ServerLevel serverLevel, ServerConfig config) {
    BlockPos origin = origin();
    BlockPos center = center();
    RadiationSources sources = RadiationSources.get(serverLevel);
    sources.remove(serverLevel, center);
    sources.addAftermath(serverLevel, center, (float) config.meltdown_radiation, config.meltdown_decay_ticks);
    Component message = Component.translatable("chat." + Faktocraft.MODID + ".nuclear_reactor.meltdown",
        center.getX(), center.getY(), center.getZ()).withStyle(ChatFormatting.RED);
    long rangeSq = (long) MELTDOWN_MESSAGE_RANGE * MELTDOWN_MESSAGE_RANGE;
    for (ServerPlayer player : serverLevel.players()) {
      if (player.blockPosition().distSqr(center) <= rangeSq) {
        PlayerMessages.display(player, message, false);
      }
    }
    NuclearReactorMultiblock.destroy(serverLevel, origin);
    serverLevel.explode(null, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5,
        (float) config.reactor_meltdown_power, true, Level.ExplosionInteraction.BLOCK);
  }

  public boolean redstoneStopped() {
    if (!isRedstoneOnly() || level == null) {
      return false;
    }
    BlockPos origin = origin();
    for (int x = 0; x < ReactorPart.SIZE; x++) {
      for (int y = 0; y < ReactorPart.SIZE; y++) {
        for (int z = 0; z < ReactorPart.SIZE; z++) {
          if (level.hasNeighborSignal(origin.offset(x, y, z))) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public boolean isRedstoneBlocked() {
    return false;
  }

  public float radiationFactor() {
    float ratio = heatRatio();
    float base = rods == 0 ? IDLE_RADIATION : running ? RUNNING_RADIATION : STANDBY_RADIATION;
    if (ratio > HOT_RATIO) {
      base += (ratio - HOT_RATIO) / (1.0F - HOT_RATIO) * (1.0F - base);
    }
    return Math.min(1.0F, base);
  }

  public int getHeat() {
    return heat;
  }

  public void setHeat(int value) {
    heat = Math.max(0, value);
    setChanged();
  }

  public int getMaxHeat() {
    return Math.max(1, ModConfig.server().reactor_max_heat);
  }

  public float heatRatio() {
    return Math.min(1.0F, heat / (float) getMaxHeat());
  }

  public boolean isRunning() {
    return running;
  }

  public int getReactionOutput() {
    return reactionOutput;
  }

  public int getOutputPerTick() {
    return outputPerTick;
  }

  public int getHeatPerTick() {
    return heatPerTick;
  }

  public int getHeatingPerTick() {
    if (!running) {
      return 0;
    }
    return throttled ? heatPerTick / SLOW_DIVISOR : heatPerTick;
  }

  public int getCoolingPerTick() {
    return coolingPerTick;
  }

  public int getRods() {
    return rods;
  }

  public int getStatus() {
    return status;
  }

  @Override
  public SoundEvent getSoundEvent() {
    return ModSounds.REACTOR_HUM;
  }

  @Override
  public float getVolume() {
    return 0.6F;
  }

  @Override
  protected boolean canPlaySound() {
    return running && soundEvent != null;
  }

  @Override
  public boolean canExtractEnergyDir(@Nullable Direction side) {
    return true;
  }

  @Override
  public int defaultGeneratorPriority() {
    return ModConfig.server().priority_nuclear_reactor;
  }

  @Override
  public List<FluidStorage> getGuiTanks() {
    return List.of(water);
  }

  @NotNull
  @Override
  public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.FLUID_HANDLER) {
      return waterCap.cast();
    }
    return super.getCapability(cap, side);
  }

  @Override
  public void setRemoved() {
    super.setRemoved();
    waterCap.invalidate();
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    tag.putInt("heat", heat);
    tag.putBoolean("running", running);
    tag.putBoolean("throttled", throttled);
    tag.putInt("reaction", reactionOutput);
    tag.putInt("output", outputPerTick);
    tag.putInt("heatRate", heatPerTick);
    tag.putInt("cooling", coolingPerTick);
    tag.putInt("rods", rods);
    tag.putInt("status", status);
    tag.putInt("burnTick", burnTick);
    CompoundTag waterTag = new CompoundTag();
    water.save(waterTag);
    tag.put("water", waterTag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    heat = tag.getIntOr("heat", 0);
    running = tag.getBooleanOr("running", false);
    throttled = tag.getBooleanOr("throttled", false);
    reactionOutput = tag.getIntOr("reaction", 0);
    outputPerTick = tag.getIntOr("output", 0);
    heatPerTick = tag.getIntOr("heatRate", 0);
    coolingPerTick = tag.getIntOr("cooling", 0);
    rods = tag.getIntOr("rods", 0);
    status = tag.getIntOr("status", 0);
    burnTick = tag.getIntOr("burnTick", 0);
    if (tag.contains("water")) {
      water.load(tag.getCompoundOrEmpty("water"));
    }
  }
}
