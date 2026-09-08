package com.faktocraft.common.entity.block;

import com.faktocraft.common.energy.impl.BasicEnergyStorage;
import com.faktocraft.common.entity.slot.FaktocraftSlot;
import com.faktocraft.common.entity.slot.SlotUpgrade;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.InventorySlotType;
import com.faktocraft.common.enums.UpgradeType;
import com.faktocraft.common.interfaces.block.IStateActive;
import com.faktocraft.common.interfaces.entity.ICooldown;
import com.faktocraft.common.interfaces.entity.IElectricSlot;
import com.faktocraft.common.interfaces.entity.IExpCollector;
import com.faktocraft.common.interfaces.entity.ISupportUpgrades;
import com.faktocraft.common.interfaces.entity.ITileSound;
import com.faktocraft.common.interfaces.item.IUpgradeItem;
import com.faktocraft.common.util.Constants;
import com.faktocraft.common.util.ItemStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaktocraftBlockEntity extends BlockEntity {

  protected boolean isActivate;
  protected boolean hasCooldown;
  protected boolean hasSound;
  protected boolean hasExp;
  protected boolean hasUpgrades;
  protected boolean hasInventory;
  protected boolean hasEnergy;
  protected boolean hasBattery;

  protected ItemStackHandler itemStackHandler;
  protected final ArrayList<FaktocraftSlot> slots = new ArrayList<>();

  protected ItemStackHandler batteryStackHandler;
  protected final ArrayList<IElectricSlot> electricSlot = new ArrayList<>();

  protected ItemStackHandler upgradeStackHandler;
  protected final ArrayList<SlotUpgrade> upgradeSlot = new ArrayList<>();

  protected BasicEnergyStorage energyStorage;

  protected int cooldown = 0;
  protected int tickCounter = 0;
  protected boolean shouldUpdateState = false;
  protected boolean activeState = false;

  private static final int ACTIVE_STATE_GRACE_TICKS = 20;
  private int activeGraceTicks = 0;

  private float speedFactor = 1;
  private float energyUsageFactor = 1;

  @Nullable
  protected SoundEvent soundEvent;

  private final Map<Integer, ItemStack> cachedInput = new HashMap<>();
  protected final Map<ResourceLocation, Integer> recipesUsed = new HashMap<>();

  private LazyOptional<IItemHandler> itemHandlerCap = LazyOptional.empty();

  public FaktocraftBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
    init();
  }

  public void init() {
    setSupportedTypes();
    initSlots();
    initBatterySlots();
    initUpgradeHandler();
    if (this instanceof ITileSound tileSound) {
      this.soundEvent = tileSound.getSoundEvent();
    }
  }

  private void setSupportedTypes() {
    this.isActivate = getBlockState().getBlock() instanceof IStateActive;
    this.hasCooldown = this instanceof ICooldown;
    this.hasSound = this instanceof ITileSound;
    this.hasExp = this instanceof IExpCollector;
    this.hasUpgrades = this instanceof ISupportUpgrades;
  }

  public void initSlots() {
    slots.clear();
    addInventorySlot(slots);
    if (!slots.isEmpty()) {
      hasInventory = true;
      itemStackHandler = new ItemStackHandler(slots.size()) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
          return isItemValidForSlot(slot, stack);
        }

        @Override
        public int getSlotLimit(int slot) {
          return getCustomSlotLimit(slot);
        }

        @Override
        protected void onContentsChanged(int slot) {
          checkInputSlotChange(slot);
          FaktocraftBlockEntity.this.setChanged();
        }
      };
      for (FaktocraftSlot slot : slots) {
        if (slot.getInventorySlotType() == InventorySlotType.INPUT) {
          cachedInput.put(slot.getSlotId(), ItemStack.EMPTY);
        }
      }

      itemHandlerCap = LazyOptional.of(() -> new InvWrapper(itemStackHandler) {
        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
          return isAutomationExtractable(slot) ? super.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }
      });
    }
  }

  private boolean isAutomationExtractable(int slotId) {
    for (FaktocraftSlot slot : slots) {
      if (slot.getSlotId() == slotId) {
        return slot.getInventorySlotType() == InventorySlotType.OUTPUT;
      }
    }
    return false;
  }

  public ArrayList<FaktocraftSlot> addInventorySlot(ArrayList<FaktocraftSlot> slots) {
    return slots;
  }

  public boolean isItemValidForSlot(int slot, ItemStack stack) {
    return false;
  }

  public int getCustomSlotLimit(int slot) {
    return 64;
  }

  public ArrayList<FaktocraftSlot> getSlots() {
    return slots;
  }

  public ItemStackHandler getItemStackHandler() {
    return itemStackHandler;
  }

  public boolean hasInventory() {
    return hasInventory;
  }

  public static final int BATTERY_DOCK_SLOTS = 4;
  public static final int TENSION_DOCK_SLOT = 2;
  private int batteryDockCapacity = 0;
  private boolean dischargeMode = false;

  public boolean isDischargeMode() {
    return dischargeMode;
  }

  public void toggleDischargeMode() {
    dischargeMode = !dischargeMode;
    if (dischargeMode) {
      setActive(false);
    }
    updateBlockState();
  }

  private static final int UNDERVOLTAGE_HOLD_TICKS = 40;
  private int undervoltageTicks = 0;

  public void markUndervoltage() {
    boolean was = isUndervoltage();
    undervoltageTicks = UNDERVOLTAGE_HOLD_TICKS;
    if (!was) {
      updateBlockState();
    }
  }

  public boolean isUndervoltage() {
    return undervoltageTicks > 0;
  }

  private void tickUndervoltage() {
    if (undervoltageTicks > 0 && --undervoltageTicks == 0) {
      updateBlockState();
    }
  }

  public boolean hasBatteryDock() {
    return hasEnergy && energyStorage != null && energyStorage.energyType() == EnergyType.RECEIVE;
  }

  public void initBatterySlots() {
    electricSlot.clear();
    if (hasBatteryDock()) {
      for (int i = 0; i < BATTERY_DOCK_SLOTS; i++) {
        int dockTop = Constants.LEFT_LAYOUT_EXPERIMENT ? 9 : 60;
        boolean batterySlot = i == BATTERY_DOCK_SLOTS - 1;
        boolean tensionSlot = i == TENSION_DOCK_SLOT;
        electricSlot.add(new com.faktocraft.common.entity.slot.SlotElectric(i, -20, dockTop + i * 18,
            batterySlot ? com.faktocraft.common.enums.InventorySlotType.BATTERY
                : tensionSlot ? com.faktocraft.common.enums.InventorySlotType.TENSION
                    : com.faktocraft.common.enums.InventorySlotType.CAPACITOR,
            batterySlot ? com.faktocraft.common.enums.GuiSlotType.DOCK_BATTERY
                : tensionSlot ? com.faktocraft.common.enums.GuiSlotType.TENSION_DOCK
                    : com.faktocraft.common.enums.GuiSlotType.BATTERY_DOCK,
            false));
      }
      hasBattery = true;
      batteryStackHandler = new ItemStackHandler(BATTERY_DOCK_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
          onBatteryDockChanged();
          FaktocraftBlockEntity.this.setChanged();
        }
      };
      onBatteryDockChanged();
      return;
    }
    addBatterySlot(electricSlot);
    if (!electricSlot.isEmpty()) {
      hasBattery = true;
      batteryStackHandler = new ItemStackHandler(electricSlot.size()) {
        @Override
        protected void onContentsChanged(int slot) {
          FaktocraftBlockEntity.this.setChanged();
        }
      };
    }
  }

  private void onBatteryDockChanged() {
    if (!hasBatteryDock() || batteryStackHandler == null) {
      return;
    }
    int cap = 0;
    for (int i = 0; i < TENSION_DOCK_SLOT; i++) {
      ItemStack stack = batteryStackHandler.getStackInSlot(i);
      if (stack.getItem() instanceof com.faktocraft.common.item.impl.CapacitorItem capacitor) {
        cap += capacitor.getCapacity();
      }
    }
    batteryDockCapacity = cap;
    energyStorage.setMaxEnergy((int) Math.min(Integer.MAX_VALUE, (long) cap));
    applyDockTension();
  }

  public int getDockTensionLevel() {
    if (batteryStackHandler == null || batteryStackHandler.getSlots() <= TENSION_DOCK_SLOT) {
      return 0;
    }
    ItemStack stack = batteryStackHandler.getStackInSlot(TENSION_DOCK_SLOT);
    return stack.getItem() instanceof com.faktocraft.common.item.impl.upgrade.TensionUpgrade tension
        ? tension.getMkLevel()
        : 0;
  }

  private void applyDockTension() {
    if (energyStorage == null) {
      return;
    }
    energyStorage.applyTierShift(getDockTensionLevel());
  }

  public int getBatteryDockCapacity() {
    return batteryDockCapacity;
  }

  public ArrayList<IElectricSlot> addBatterySlot(ArrayList<IElectricSlot> slots) {
    return slots;
  }

  public ItemStackHandler getBatteryStackHandler() {
    return batteryStackHandler;
  }

  public ArrayList<IElectricSlot> getElectricSlot() {
    return electricSlot;
  }

  public boolean hasBattery() {
    return hasBattery;
  }

  public void initUpgradeHandler() {
    upgradeSlot.clear();
    if (hasUpgrades()) {
      for (int i = 0; i < 4; i++) {
        upgradeSlot.add(new SlotUpgrade(i, 178, 9 + (i * 18)));
      }
      upgradeStackHandler = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
          FaktocraftBlockEntity.this.setChanged();
        }
      };
    }
  }

  public boolean hasUpgrades() {
    return hasUpgrades;
  }

  public List<UpgradeType> getSupportedUpgrades() {
    return List.of();
  }

  public ItemStackHandler getUpgradeStackHandler() {
    return upgradeStackHandler;
  }

  public ArrayList<SlotUpgrade> getUpgradeSlot() {
    return upgradeSlot;
  }

  public void createEnergyStorage(int energyStored, int maxEnergy, EnergyType energyType, EnergyTier energyTier) {
    createEnergyStorage(energyStored, maxEnergy, energyType, java.util.EnumSet.of(energyTier));
  }

  public void createEnergyStorage(int energyStored, int maxEnergy, EnergyType energyType,
      EnergyTier firstTier, EnergyTier... moreTiers) {
    java.util.EnumSet<EnergyTier> tiers = java.util.EnumSet.of(firstTier);
    java.util.Collections.addAll(tiers, moreTiers);
    createEnergyStorage(energyStored, maxEnergy, energyType, tiers);
  }

  public void createEnergyStorage(int energyStored, int maxEnergy, EnergyType energyType,
      java.util.Set<EnergyTier> energyTiers) {
    this.energyStorage = new BasicEnergyStorage(energyStored, maxEnergy, energyType, energyTiers) {
      @Override
      public boolean canExtractEnergy(@Nullable Direction side) {
        return dischargeMode || canExtractEnergyDir(side);
      }

      @Override
      public boolean canReceiveEnergy(@Nullable Direction side) {
        return !dischargeMode && canReceiveEnergyDir(side);
      }

      @Override
      public int maxReceiveTick() {
        if (dischargeMode) {
          return 0;
        }
        int custom = customEnergyReceiveTick();
        return custom != -1 ? custom : super.maxReceiveTick();
      }

      @Override
      public int maxExtractTick() {
        if (dischargeMode) {
          return energyTier().getBasicTransfer();
        }
        int custom = customEnergyExtractTick();
        return custom != -1 ? custom : super.maxExtractTick();
      }

      @Override
      public void updated() {
        setChanged();
        shouldUpdateState = true;
      }
    };
    this.hasEnergy = true;
    if (energyType == EnergyType.RECEIVE) {
      initBatterySlots();
    }
  }

  public BasicEnergyStorage getEnergyStorage() {
    return energyStorage;
  }

  public boolean hasEnergy() {
    return hasEnergy;
  }

  public boolean canExtractEnergyDir(@Nullable Direction side) {
    return false;
  }

  public boolean canReceiveEnergyDir(@Nullable Direction side) {
    return false;
  }

  public int customEnergyReceiveTick() {
    return -1;
  }

  public int customEnergyExtractTick() {
    return -1;
  }

  public float getSpeedFactor() {
    return speedFactor;
  }

  public float getEnergyUsageFactor() {
    return energyUsageFactor;
  }

  public int getRedstonePower() {
    if (level == null) {
      return 0;
    }
    return level.getDirectSignalTo(getBlockPos());
  }

  private boolean redstoneOnly = false;

  public boolean isRedstoneOnly() {
    return redstoneOnly;
  }

  public void setRedstoneOnly(boolean redstoneOnly) {
    this.redstoneOnly = redstoneOnly;
    setChanged();
  }

  public boolean supportsRedstoneControl() {
    return true;
  }

  public boolean isRedstoneBlocked() {
    return redstoneOnly && level != null && !level.hasNeighborSignal(getBlockPos());
  }

  private int generatorPriorityMode = 0;

  public boolean isGenerator() {
    return hasEnergy && energyStorage.energyType() == com.faktocraft.common.enums.EnergyType.EXTRACT;
  }

  public int getGeneratorPriorityMode() {
    return generatorPriorityMode;
  }

  public void setGeneratorPriorityMode(int mode) {
    this.generatorPriorityMode = net.minecraft.util.Mth.clamp(mode, 0, 5);
    setChanged();
  }

  public int defaultGeneratorPriority() {
    return 3;
  }

  public int effectiveGeneratorPriority() {
    return generatorPriorityMode != 0 ? generatorPriorityMode
        : net.minecraft.util.Mth.clamp(defaultGeneratorPriority(), 1, 5);
  }

  public java.util.List<FluidStorage> getGuiTanks() {
    return java.util.List.of();
  }

  public void tickUpgrades(BlockState state) {
    if (upgradeStackHandler == null) {
      return;
    }

    int overclockers = 0;
    int efficiency = 0;

    for (int i = 0; i < upgradeStackHandler.getSlots(); i++) {
      ItemStack stack = upgradeStackHandler.getStackInSlot(i);
      if (stack.isEmpty() || !(stack.getItem() instanceof IUpgradeItem upgradeItem)) {
        continue;
      }
      if (upgradeItem.getUpgradeType() == UpgradeType.OVERCLOCKER) {
        overclockers += stack.getCount() * (upgradeItem.isAdvancedUpgrade() ? 3 : 1);
      } else if (upgradeItem.getUpgradeType() == UpgradeType.EFFICIENCY) {
        efficiency += stack.getCount() * (upgradeItem.isAdvancedUpgrade() ? 3 : 1);
      }
    }

    int effectiveOverclock = Math.min(overclockers, 8);
    int effectiveEfficiency = Math.min(efficiency, 8);
    speedFactor = (float) Math.pow(0.7, effectiveOverclock);
    energyUsageFactor = (float) (Math.pow(1.6, effectiveOverclock) * Math.pow(0.85, effectiveEfficiency));

    if (hasEnergy && energyStorage != null) {
      long newMax;
      if (hasBatteryDock()) {
        newMax = batteryDockCapacity;
      } else {

        newMax = (long) (energyStorage.origEnergy * Math.max(1.0F, energyUsageFactor));
      }
      int clamped = (int) Math.min(Integer.MAX_VALUE, newMax);
      if (energyStorage.maxEnergy() != clamped) {
        energyStorage.setMaxEnergy(clamped);
      }
    }
  }

  protected IItemHandler createSlotTypeStorage(InventorySlotType... types) {
    List<InventorySlotType> typeList = List.of(types);
    return new InvWrapper(new SlotTypeContainer(typeList));
  }

  private class SlotTypeContainer implements net.minecraft.world.Container {
    private final List<Integer> slotIds = new ArrayList<>();

    SlotTypeContainer(List<InventorySlotType> types) {
      for (FaktocraftSlot slot : slots) {
        if (types.contains(slot.getInventorySlotType())) {
          slotIds.add(slot.getSlotId());
        }
      }
    }

    @Override
    public int getContainerSize() {
      return slotIds.size();
    }

    @Override
    public boolean isEmpty() {
      for (int id : slotIds) {
        if (!itemStackHandler.getStackInSlot(id).isEmpty()) {
          return false;
        }
      }
      return true;
    }

    @Override
    public ItemStack getItem(int slot) {
      return itemStackHandler.getStackInSlot(slotIds.get(slot));
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
      return itemStackHandler.extractItem(slotIds.get(slot), amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
      return itemStackHandler.removeItemNoUpdate(slotIds.get(slot));
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
      itemStackHandler.setStackInSlot(slotIds.get(slot), stack);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
      return isItemValidForSlot(slotIds.get(slot), stack);
    }

    @Override
    public void setChanged() {
      FaktocraftBlockEntity.this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
      return true;
    }

    @Override
    public void clearContent() {
    }
  }

  public int getCooldown() {
    return cooldown;
  }

  public void setCooldown(int time) {
    this.cooldown = time;
    setChanged();
  }

  protected boolean getActive() {
    return isActivate && getBlockState().getBlock() instanceof IStateActive active && active.isActive(getBlockState());
  }

  public boolean setActive(boolean active) {
    this.activeState = active;
    if (isActivate && level != null && !level.isClientSide()) {
      if (active) {
        activeGraceTicks = ACTIVE_STATE_GRACE_TICKS;
        if (!getActive() && getBlockState().getBlock() instanceof IStateActive) {
          setActiveState(true);
        }
      }
    }
    return active;
  }

  protected void setActiveState(boolean active) {
    if (level == null) {
      return;
    }
    if (getBlockState().getBlock() instanceof IStateActive stateActive) {
      BlockState newState = stateActive.setActive(getBlockState(), active);
      level.setBlockAndUpdate(getBlockPos(), newState);
    }
  }

  public void tickServer(BlockState state) {
    if (hasEnergy && energyStorage != null) {
      energyStorage.tickEnergyStats();
    }

    boolean externalUpdate = shouldUpdateState;
    shouldUpdateState = false;
    activeState = false;

    tickUndervoltage();

    tickCounter++;
    if (tickCounter > 20) {
      tickCounter = 0;
    }

    if (hasCooldown && tickCounter == 20 && cooldown > 0) {
      cooldown--;
      updateBlockState();
    }

    if (hasUpgrades()) {
      tickUpgrades(state);
    }

    if (!dischargeMode && !isRedstoneBlocked()) {
      tickWork(state);
    }

    if (isActivate && !activeState && getActive()) {
      if (activeGraceTicks > 0) {
        activeGraceTicks--;
      } else {
        setActiveState(false);
      }
    }

    if (shouldUpdateState || externalUpdate) {
      updateBlockState();
    }

    flushPendingWorldBroadcast();
  }

  public void tickWork(BlockState state) {
  }

  public void tickClient(BlockState state) {
    if (hasSound) {
      handleSound();
    }
  }

  protected boolean canPlaySound() {
    return getActive() && soundEvent != null;
  }

  private void handleSound() {
    if (level == null || !level.isClientSide()) {
      return;
    }
    if (tickCounter == 0) {
      if (canPlaySound() && !isRemoved()) {
        com.faktocraft.client.SoundHandler.startTileSound(soundEvent, getSoundCategoryVolume(), getBlockPos());
      } else {
        com.faktocraft.client.SoundHandler.stopTileSound(getBlockPos());
      }
    }
    tickCounter++;
    if (tickCounter > 20) {
      tickCounter = 0;
    }
  }

  private float getSoundCategoryVolume() {
    return this instanceof ITileSound tileSound ? tileSound.getVolume() : 1.0F;
  }

  private long lastWorldBroadcastTick = -1_000_000L;
  private boolean pendingWorldBroadcast = false;
  private long guiChangeTick = 0;

  protected int worldSyncIntervalTicks() {
    return 10;
  }

  public long getGuiChangeTick() {
    return guiChangeTick;
  }

  public void updateBlockState() {
    setChanged();
    if (level == null || level.isClientSide()) {
      return;
    }
    long now = level.getGameTime();
    guiChangeTick = now;
    if (now - lastWorldBroadcastTick >= worldSyncIntervalTicks()) {
      broadcastToWorld(now);
    } else {
      pendingWorldBroadcast = true;
    }
  }

  private void broadcastToWorld(long now) {
    lastWorldBroadcastTick = now;
    pendingWorldBroadcast = false;
    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
  }

  private void flushPendingWorldBroadcast() {
    if (pendingWorldBroadcast && level != null
        && level.getGameTime() - lastWorldBroadcastTick >= worldSyncIntervalTicks()) {
      broadcastToWorld(level.getGameTime());
    }
  }

  @Override
  public CompoundTag getUpdateTag() {
    CompoundTag tag = new CompoundTag();
    saveAdditional(tag);
    return tag;
  }

  @Nullable
  @Override
  public Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  protected void seedInputCache(int slotId) {
    if (itemStackHandler != null && cachedInput.containsKey(slotId)) {
      cachedInput.put(slotId, itemStackHandler.getStackInSlot(slotId).copy());
    }
  }

  protected boolean checkInputSlotChange(int slotId) {
    if (!cachedInput.containsKey(slotId)) {
      return false;
    }
    ItemStack oldStack = cachedInput.get(slotId);
    ItemStack newStack = itemStackHandler.getStackInSlot(slotId);
    if (!ItemStack.matches(oldStack, newStack)) {
      boolean result = inputSlotChanged(slotId, oldStack, newStack);
      cachedInput.put(slotId, newStack.copy());
      return result;
    }
    return false;
  }

  public boolean inputSlotChanged(int slotId, ItemStack oldStack, ItemStack newStack) {
    return false;
  }

  public float getExperience(Recipe<?> recipe) {
    return 0;
  }

  public void addRecipeUsed(@Nullable Recipe<?> recipe) {
    if (recipe != null) {
      recipesUsed.merge(recipe.getId(), 1, Integer::sum);
    }
  }

  public float getStoredExperience() {
    if (level == null) {
      return 0;
    }
    float total = 0;
    if (level instanceof ServerLevel serverLevel) {
      for (Map.Entry<ResourceLocation, Integer> entry : recipesUsed.entrySet()) {
        total += serverLevel.getRecipeManager().byKey(entry.getKey())
            .map(recipe -> getExperience(recipe) * entry.getValue())
            .orElse(0F);
      }
    }
    return total;
  }

  protected void awardStoredExperience(ServerLevel serverLevel) {
    float storedExp = getStoredExperience();
    if (storedExp > 0) {
      Vec3 pos = Vec3.atCenterOf(getBlockPos());
      int fullExp = Mth.floor(storedExp);
      float fraction = Mth.frac(storedExp);
      if (fraction != 0.0F && Math.random() < fraction) {
        fullExp++;
      }
      ExperienceOrb.award(serverLevel, pos, fullExp);
    }
    recipesUsed.clear();
  }

  public void collectExp(Player player) {
    if (level instanceof ServerLevel serverLevel) {
      awardStoredExperience(serverLevel);
      updateBlockState();
    }
  }

  public void onPlace(boolean isClient) {
  }

  public void onBreak(boolean isClient) {
    if (isClient) {
      onBreakClient();
    } else {
      onBreakServer();
    }
  }

  public void onBreakClient() {
  }

  public void onBreakServer() {
    if (level == null) {
      return;
    }
    if (hasExp && !recipesUsed.isEmpty() && level instanceof ServerLevel serverLevel) {
      awardStoredExperience(serverLevel);
    }
    List<ItemStack> drops = new ArrayList<>();
    if (hasInventory) {
      for (FaktocraftSlot slot : slots) {
        if (slot.getInventorySlotType() != InventorySlotType.DISABLED) {
          drops.add(itemStackHandler.getStackInSlot(slot.getSlotId()));
        }
      }
    }
    if (hasBattery) {
      for (int i = 0; i < batteryStackHandler.getSlots(); i++) {
        drops.add(batteryStackHandler.getStackInSlot(i));
      }
    }
    if (hasUpgrades() && upgradeStackHandler != null) {
      for (int i = 0; i < upgradeStackHandler.getSlots(); i++) {
        drops.add(upgradeStackHandler.getStackInSlot(i));
      }
    }
    for (ItemStack stack : drops) {
      if (!stack.isEmpty()) {
        Containers.dropItemStack(level, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), stack);
      }
    }
  }

  public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    if (level != null) {
      onBreak(level.isClientSide());
    }
  }

  @Override
  public void onLoad() {
    super.onLoad();

    if (level != null && !level.isClientSide()
        && this instanceof com.faktocraft.common.energy.interfaces.IEnergyBlock) {
      com.faktocraft.common.energy.provider.EnergyCore.get(level).addEnergyBlock(worldPosition);
    }
  }

  @Override
  public void setRemoved() {
    super.setRemoved();
    itemHandlerCap.invalidate();
    if (level != null && level.isClientSide()) {
      com.faktocraft.client.SoundHandler.stopTileSound(getBlockPos());
    }
  }

  @NotNull
  @Nullable
  public Direction getFacing() {
    return getBlockState().getBlock() instanceof com.faktocraft.common.interfaces.block.IStateFacing facing
        ? facing.getDirection(getBlockState())
        : null;
  }

  public boolean isFrontSide(@Nullable Direction side) {
    return side != null && side == getFacing();
  }

  @Override
  public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.ITEM_HANDLER && hasInventory && itemStackHandler != null) {
      if (isFrontSide(side)) {
        return LazyOptional.empty();
      }
      return itemHandlerCap.cast();
    }
    return super.getCapability(cap, side);
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    if (isActivate) {
      tag.putBoolean("activeState", activeState);
    }
    if (hasInventory) {
      CompoundTag inventory = new CompoundTag();
      itemStackHandler.save(inventory);
      tag.put("inventory", inventory);
    }
    if (hasBattery) {
      CompoundTag battery = new CompoundTag();
      batteryStackHandler.save(battery);
      tag.put("battery", battery);
    }
    if (hasUpgrades() && upgradeStackHandler != null) {
      CompoundTag upgrade = new CompoundTag();
      upgradeStackHandler.save(upgrade);
      tag.put("upgrade", upgrade);
    }
    if (hasEnergy) {
      tag.putInt("energy", energyStorage.energyStored());
      tag.putBoolean("dischargeMode", dischargeMode);
      tag.putBoolean("undervoltage", isUndervoltage());
    }
    if (hasCooldown) {
      tag.putInt("cooldown", cooldown);
    }
    if (redstoneOnly) {
      tag.putBoolean("redstoneOnly", true);
    }
    if (generatorPriorityMode != 0) {
      tag.putInt("generatorPriority", generatorPriorityMode);
    }
    if (hasExp) {
      CompoundTag expOutput = new CompoundTag();
      for (Map.Entry<ResourceLocation, Integer> entry : recipesUsed.entrySet()) {
        expOutput.putInt(entry.getKey().toString(), entry.getValue());
      }
      tag.put("recipesUsed", expOutput);
    }
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    if (isActivate) {
      activeState = tag.getBoolean("activeState");
    }
    if (hasInventory && tag.contains("inventory")) {
      itemStackHandler.load(tag.getCompound("inventory"));
    }
    if (hasBattery && tag.contains("battery")) {
      batteryStackHandler.load(tag.getCompound("battery"));
      if (hasBatteryDock()) {
        onBatteryDockChanged();
      }
    }
    if (hasUpgrades() && upgradeStackHandler != null && tag.contains("upgrade")) {
      upgradeStackHandler.load(tag.getCompound("upgrade"));
    }
    if (hasEnergy) {
      energyStorage.setEnergy(tag.contains("energy") ? tag.getInt("energy") : 0);
      dischargeMode = tag.getBoolean("dischargeMode");
      undervoltageTicks = tag.getBoolean("undervoltage") ? UNDERVOLTAGE_HOLD_TICKS : 0;
    }
    redstoneOnly = tag.getBoolean("redstoneOnly");
    generatorPriorityMode = net.minecraft.util.Mth.clamp(tag.getInt("generatorPriority"), 0, 5);
    if (hasCooldown) {
      cooldown = tag.contains("cooldown") ? tag.getInt("cooldown") : 0;
    }
    if (hasExp && tag.contains("recipesUsed")) {
      recipesUsed.clear();
      CompoundTag expTag = tag.getCompound("recipesUsed");
      for (String key : expTag.getAllKeys()) {
        recipesUsed.put(new ResourceLocation(key), expTag.getInt(key));
      }
    }
  }
}
