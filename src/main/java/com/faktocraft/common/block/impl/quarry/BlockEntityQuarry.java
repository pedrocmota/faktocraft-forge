package com.faktocraft.common.block.impl.quarry;

import com.faktocraft.common.block.impl.pipe.PipeExtractor;
import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.interfaces.block.IStateFacing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockEntityQuarry extends FaktocraftBlockEntity implements IEnergyBlock {

  public static final int INVENTORY_SLOTS = 27;
  public static final int UPGRADE_SLOTS = 4;
  public static final int FRAME_HEIGHT_ABOVE = 4;
  public static final int DEFAULT_FRAME_SPAN = 11;
  public static final int MAX_FRAME_SPAN = 30;
  public static final int MIN_FRAME_SPAN = 10;

  public static final TagKey<Block> QUARRY_MINEABLE = TagKey.create(Registries.BLOCK,
      new ResourceLocation("faktocraft", "quarry_mineable"));

  public static final int STAGE_AREA = 0;
  public static final int STAGE_CLEAR = 1;
  public static final int STAGE_FRAME = 2;
  public static final int STAGE_MINE = 3;
  public static final int STAGE_DONE = 4;

  public static final int STATUS_OFF = 0;
  public static final int STATUS_INVALID_AREA = 1;
  public static final int STATUS_CLEARING = 2;
  public static final int STATUS_FRAMING = 3;
  public static final int STATUS_MINING = 4;
  public static final int STATUS_OBSTRUCTED = 5;
  public static final int STATUS_FULL = 6;
  public static final int STATUS_NO_ENERGY = 7;
  public static final int STATUS_WAITING_CHUNKS = 8;
  public static final int STATUS_DONE = 9;

  private static final int SCAN_BUDGET = 128;
  private static final int REPLACEABLE_BUDGET = 16;
  private static final int AREA_RETRY_TICKS = 60;
  private static final float ARM_BASE_SPEED = 0.25F;
  private static final float GLOBAL_SPEED = 1.1F;

  private static final float HEAD_HOVER = 1.03F;
  private static final int DRILL_DWELL_TICKS = 2;
  private static final ItemStack MINING_TOOL = new ItemStack(Items.DIAMOND_PICKAXE);

  private final com.faktocraft.common.util.ItemStackHandler inventory = new com.faktocraft.common.util.ItemStackHandler(
      INVENTORY_SLOTS) {
    @Override
    protected void onContentsChanged(int slot) {
      BlockEntityQuarry.this.setChanged();
    }
  };

  private final com.faktocraft.common.util.ItemStackHandler upgrades = new com.faktocraft.common.util.ItemStackHandler(
      UPGRADE_SLOTS) {
    @Override
    protected void onContentsChanged(int slot) {
      BlockEntityQuarry.this.setChanged();
    }
  };

  private final IItemHandler extractOnly = new IItemHandler() {
    @Override
    public int getSlots() {
      return inventory.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
      return inventory.getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
      return stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
      return inventory.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
      return inventory.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
      return false;
    }
  };

  private final LazyOptional<IItemHandler> extractOnlyCap = LazyOptional.of(() -> extractOnly);

  private int runMode = PipeExtractor.RUN_ALWAYS;
  private boolean areaSet = false;
  private int minX;
  private int minZ;
  private int maxX;
  private int maxZ;
  private int stage = STAGE_AREA;
  private int status = STATUS_OFF;
  private int clearIndex = 0;
  private int frameIndex = 0;
  private long mineIndex = 0;
  private int progress = 0;
  private int areaRetry = 0;
  private int syncTick = 0;

  private boolean targetValid = false;
  private int drillTicks = 0;
  private int targetX;
  private int targetY;
  private int targetZ;

  public float headX;
  public float headY;
  public float headZ;

  public float clientHeadX;
  public float clientHeadY;
  public float clientHeadZ;
  public double clientHeadLastTime = Double.NaN;

  private List<BlockPos> framePositions;

  public BlockEntityQuarry(BlockPos pos, BlockState state) {
    super(QuarryRegistry.QUARRY_BLOCK_ENTITY, pos, state);
    createEnergyStorage(0, ModConfig.server().quarry_energy_capacity, EnergyType.RECEIVE, EnergyTier.HIGH);
    headX = pos.getX() + 0.5F;
    headY = pos.getY() + FRAME_HEIGHT_ABOVE + 0.5F;
    headZ = pos.getZ() + 0.5F;
  }

  @Override
  public boolean supportsRedstoneControl() {
    return false;
  }

  @Override
  protected int worldSyncIntervalTicks() {
    return 1;
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable Direction side) {
    return true;
  }

  public com.faktocraft.common.util.ItemStackHandler getInventory() {
    return inventory;
  }

  public com.faktocraft.common.util.ItemStackHandler getUpgrades() {
    return upgrades;
  }

  public int getRunMode() {
    return runMode;
  }

  public void setRunMode(int runMode) {
    this.runMode = net.minecraft.util.Mth.clamp(runMode, 0, 2);
    setChanged();
  }

  public int getStatus() {
    return status;
  }

  public void setStatusClient(int value) {
    status = value;
  }

  public boolean hasArea() {
    return areaSet;
  }

  public int stage() {
    return stage;
  }

  public int areaMinX() {
    return minX;
  }

  public int areaMinZ() {
    return minZ;
  }

  public int areaMaxX() {
    return maxX;
  }

  public int areaMaxZ() {
    return maxZ;
  }

  private boolean allowedToRun() {
    return runMode != PipeExtractor.RUN_OFF
        && !(runMode == PipeExtractor.RUN_REDSTONE && getRedstonePower() <= 0);
  }

  private int upgradePoints(com.faktocraft.common.enums.UpgradeType type) {
    int points = 0;
    for (int i = 0; i < upgrades.getSlots(); i++) {
      var stack = upgrades.getStackInSlot(i);
      if (stack.getItem() instanceof com.faktocraft.common.interfaces.item.IUpgradeItem upgrade
          && upgrade.getUpgradeType() == type) {
        points += upgrade.isAdvancedUpgrade() ? 3 : 1;
      }
    }
    return Math.min(points, 8);
  }

  private int boostPoints() {
    return upgradePoints(com.faktocraft.common.enums.UpgradeType.OVERCLOCKER);
  }

  private int efficiencyPoints() {
    return upgradePoints(com.faktocraft.common.enums.UpgradeType.EFFICIENCY);
  }

  public int energyPerBlock() {
    return (int) Math.ceil(Math.max(1, ModConfig.server().quarry_energy_per_block)
        * Math.pow(1.6, boostPoints()) * Math.pow(0.85, efficiencyPoints()));
  }

  public int maxDrawPerTick() {
    return (int) Math.ceil(Math.max(1, ModConfig.server().quarry_max_draw_per_tick) * GLOBAL_SPEED
        * Math.pow(1.6 / 0.65, boostPoints()) * Math.pow(0.85, efficiencyPoints()));
  }

  public float armSpeed() {
    return GLOBAL_SPEED * Math.min(1.2F, ARM_BASE_SPEED * (1.0F + 0.35F * boostPoints()));
  }

  @Override
  public void tickWork(BlockState state) {
    getEnergyStorage().updateConsumed(0);
    if (!(level instanceof ServerLevel serverLevel)) {
      return;
    }

    int newStatus;
    boolean worked = false;
    if (!allowedToRun()) {
      newStatus = STATUS_OFF;
    } else if (!areaSet) {
      newStatus = initArea(serverLevel) ? statusForStage() : STATUS_INVALID_AREA;
    } else {
      newStatus = switch (stage) {
        case STAGE_CLEAR -> tickClear(serverLevel);
        case STAGE_FRAME -> tickFrame(serverLevel);
        case STAGE_MINE -> tickMine(serverLevel);
        default -> STATUS_DONE;
      };
      worked = newStatus == STATUS_CLEARING || newStatus == STATUS_FRAMING || newStatus == STATUS_MINING;
    }

    moveHead();

    if (newStatus != status) {
      status = newStatus;
      updateBlockState();
    } else if ((syncTick++ & 1) == 0) {
      updateBlockState();
    }
    if (this.setActive(worked)) {
      updateBlockState();
    }
  }

  private int statusForStage() {
    return switch (stage) {
      case STAGE_CLEAR -> STATUS_CLEARING;
      case STAGE_FRAME -> STATUS_FRAMING;
      case STAGE_MINE -> STATUS_MINING;
      case STAGE_DONE -> STATUS_DONE;
      default -> STATUS_INVALID_AREA;
    };
  }

  private boolean initArea(ServerLevel serverLevel) {
    if (areaRetry > 0) {
      areaRetry--;
      return false;
    }
    areaRetry = AREA_RETRY_TICKS;

    int[] rect = BlockLandmark.rectAround(serverLevel, worldPosition);
    boolean marked = rect != null;
    if (!marked && BlockLandmark.touchesLandmarks(serverLevel, worldPosition)) {
      return false;
    }

    int rectMinX;
    int rectMaxX;
    int rectMinZ;
    int rectMaxZ;
    if (marked) {
      rectMinX = rect[0];
      rectMaxX = rect[1];
      rectMinZ = rect[2];
      rectMaxZ = rect[3];
    } else {
      Direction front = getBlockState().getBlock() instanceof IStateFacing facing
          ? facing.getDirection(getBlockState()).getOpposite()
          : Direction.NORTH;
      int half = DEFAULT_FRAME_SPAN / 2;
      BlockPos near = worldPosition.relative(front);
      BlockPos far = worldPosition.relative(front, DEFAULT_FRAME_SPAN);
      rectMinX = Math.min(near.getX(), far.getX()) - (front.getAxis() == Direction.Axis.Z ? half : 0);
      rectMaxX = Math.max(near.getX(), far.getX()) + (front.getAxis() == Direction.Axis.Z ? half : 0);
      rectMinZ = Math.min(near.getZ(), far.getZ()) - (front.getAxis() == Direction.Axis.X ? half : 0);
      rectMaxZ = Math.max(near.getZ(), far.getZ()) + (front.getAxis() == Direction.Axis.X ? half : 0);
    }

    int width = rectMaxX - rectMinX + 1;
    int depth = rectMaxZ - rectMinZ + 1;
    boolean quarryInside = worldPosition.getX() >= rectMinX && worldPosition.getX() <= rectMaxX
        && worldPosition.getZ() >= rectMinZ && worldPosition.getZ() <= rectMaxZ;
    if (width < MIN_FRAME_SPAN || depth < MIN_FRAME_SPAN
        || width > MAX_FRAME_SPAN || depth > MAX_FRAME_SPAN || quarryInside) {
      return false;
    }
    if (volumeHasForeignStructure(serverLevel, rectMinX, rectMaxX, rectMinZ, rectMaxZ)) {
      return false;
    }

    minX = rectMinX;
    maxX = rectMaxX;
    minZ = rectMinZ;
    maxZ = rectMaxZ;
    areaSet = true;
    stage = STAGE_CLEAR;
    clearIndex = 0;
    framePositions = null;

    if (marked) {
      popLandmarks(serverLevel);
    }
    setChanged();
    updateBlockState();
    return true;
  }

  private boolean volumeHasForeignStructure(ServerLevel serverLevel, int rectMinX, int rectMaxX,
      int rectMinZ, int rectMaxZ) {
    BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    for (int x = rectMinX; x <= rectMaxX; x++) {
      for (int z = rectMinZ; z <= rectMaxZ; z++) {
        for (int y = worldPosition.getY(); y <= worldPosition.getY() + FRAME_HEIGHT_ABOVE; y++) {
          cursor.set(x, y, z);
          BlockState state = serverLevel.getBlockState(cursor);
          if (state.is(QuarryRegistry.QUARRY_FRAME)
              || (state.is(QuarryRegistry.QUARRY) && !cursor.equals(worldPosition))) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private void popLandmarks(ServerLevel serverLevel) {
    int y = worldPosition.getY();
    BlockPos[] corners = {
        new BlockPos(minX, y, minZ), new BlockPos(maxX, y, minZ),
        new BlockPos(minX, y, maxZ), new BlockPos(maxX, y, maxZ) };
    for (BlockPos corner : corners) {
      if (serverLevel.getBlockState(corner).is(QuarryRegistry.LANDMARK)) {
        serverLevel.removeBlock(corner, false);
        ItemStack leftover = insertStacked(inventory, new ItemStack(QuarryRegistry.LANDMARK_ITEM));
        if (!leftover.isEmpty()) {
          Containers.dropItemStack(serverLevel, corner.getX() + 0.5, corner.getY() + 0.5, corner.getZ() + 0.5,
              leftover);
        }
      }
    }
  }

  private int tickClear(ServerLevel serverLevel) {
    int width = maxX - minX + 1;
    int depth = maxZ - minZ + 1;
    int perLayer = width * depth;
    int total = perLayer * (FRAME_HEIGHT_ABOVE + 1);
    int replaceableBudget = REPLACEABLE_BUDGET;

    while (clearIndex < total) {
      int layer = clearIndex / perLayer;
      int inLayer = clearIndex % perLayer;
      BlockPos pos = new BlockPos(minX + inLayer % width, worldPosition.getY() + layer,
          minZ + inLayer / width);
      if (pos.equals(worldPosition)) {
        clearIndex++;
        continue;
      }
      if (!serverLevel.isLoaded(pos)) {
        return STATUS_WAITING_CHUNKS;
      }
      BlockState state = serverLevel.getBlockState(pos);
      if (state.isAir()
          || (!state.getFluidState().isEmpty() && state.getBlock() instanceof LiquidBlock)) {
        clearIndex++;
        continue;
      }
      if (state.canBeReplaced()) {
        serverLevel.destroyBlock(pos, false);
        clearIndex++;
        if (replaceableBudget-- <= 0) {
          return STATUS_CLEARING;
        }
        continue;
      }
      if (isMineable(serverLevel, pos, state)) {
        return mineAt(serverLevel, pos, state, STATUS_CLEARING, () -> clearIndex++);
      }
      boolean onRing = pos.getX() == minX || pos.getX() == maxX || pos.getZ() == minZ || pos.getZ() == maxZ;
      if (onRing) {
        return STATUS_OBSTRUCTED;
      }
      clearIndex++;
    }
    stage = STAGE_FRAME;
    frameIndex = 0;
    setChanged();
    return STATUS_FRAMING;
  }

  private List<BlockPos> framePositions() {
    if (framePositions != null) {
      return framePositions;
    }
    List<BlockPos> list = new ArrayList<>();
    int y = worldPosition.getY();
    int top = y + FRAME_HEIGHT_ABOVE;
    for (int x = minX; x <= maxX; x++) {
      for (int z = minZ; z <= maxZ; z++) {
        boolean onRing = x == minX || x == maxX || z == minZ || z == maxZ;
        boolean corner = (x == minX || x == maxX) && (z == minZ || z == maxZ);
        if (onRing) {
          list.add(new BlockPos(x, y, z));
          list.add(new BlockPos(x, top, z));
        }
        if (corner) {
          for (int py = y + 1; py < top; py++) {
            list.add(new BlockPos(x, py, z));
          }
        }
      }
    }
    framePositions = list;
    return list;
  }

  private int tickFrame(ServerLevel serverLevel) {
    List<BlockPos> positions = framePositions();
    int placed = 0;
    while (frameIndex < positions.size() && placed < 2) {
      BlockPos pos = positions.get(frameIndex);
      if (pos.equals(worldPosition)) {
        frameIndex++;
        continue;
      }
      if (!serverLevel.isLoaded(pos)) {
        return STATUS_WAITING_CHUNKS;
      }
      BlockState state = serverLevel.getBlockState(pos);
      if (state.is(QuarryRegistry.QUARRY_FRAME)) {
        frameIndex++;
        continue;
      }
      if (state.isAir() || state.canBeReplaced()
          || (!state.getFluidState().isEmpty() && state.getBlock() instanceof LiquidBlock)) {
        serverLevel.setBlock(pos, BlockQuarryFrame.connectedState(
            QuarryRegistry.QUARRY_FRAME.defaultBlockState(), serverLevel, pos), 3);
        frameIndex++;
        placed++;
        continue;
      }
      return STATUS_OBSTRUCTED;
    }
    if (frameIndex >= positions.size()) {
      stage = STAGE_MINE;
      mineIndex = 0;
      targetValid = false;
      setChanged();
      return STATUS_MINING;
    }
    return STATUS_FRAMING;
  }

  private int tickMine(ServerLevel serverLevel) {
    int width = maxX - minX - 1;
    int depth = maxZ - minZ - 1;
    long perLayer = (long) width * depth;
    int topY = worldPosition.getY() - 1;
    int bottomY = serverLevel.getMinBuildHeight();
    long total = perLayer * (topY - bottomY + 1);

    if (!targetValid) {
      int budget = SCAN_BUDGET;
      while (mineIndex < total && budget-- > 0) {
        long layer = mineIndex / perLayer;
        int inLayer = (int) (mineIndex % perLayer);
        BlockPos pos = new BlockPos(minX + 1 + inLayer % width, topY - (int) layer,
            minZ + 1 + inLayer / width);
        if (!serverLevel.isLoaded(pos)) {
          return STATUS_WAITING_CHUNKS;
        }
        BlockState state = serverLevel.getBlockState(pos);

        if (isMineable(serverLevel, pos, state)
            || state.canBeReplaced() && !state.isAir() && state.getFluidState().isEmpty()) {
          targetValid = true;
          targetX = pos.getX();
          targetY = pos.getY();
          targetZ = pos.getZ();
          break;
        }
        mineIndex++;
      }
      if (!targetValid) {
        if (mineIndex >= total) {
          stage = STAGE_DONE;
          setChanged();
          return STATUS_DONE;
        }
        return STATUS_MINING;
      }
    }

    BlockPos pos = new BlockPos(targetX, targetY, targetZ);
    if (!serverLevel.isLoaded(pos)) {
      return STATUS_WAITING_CHUNKS;
    }
    BlockState state = serverLevel.getBlockState(pos);
    if (!isMineable(serverLevel, pos, state) && !(state.canBeReplaced() && !state.isAir())) {
      targetValid = false;
      mineIndex++;
      return STATUS_MINING;
    }

    int cost = state.canBeReplaced() ? 0 : energyPerBlock();
    int result = STATUS_MINING;
    if (progress < cost) {
      int draw = Math.min(maxDrawPerTick(), cost - progress);
      int accepted = getEnergyStorage().consumeEnergy(draw, true);
      if (accepted > 0) {
        getEnergyStorage().consumeEnergy(accepted, false);
        getEnergyStorage().updateConsumed(accepted);
        progress += accepted;
      } else {
        result = STATUS_NO_ENERGY;
      }
    }

    if (progress >= cost && headAtTarget()) {
      if (drillTicks < DRILL_DWELL_TICKS) {
        drillTicks++;
        return STATUS_MINING;
      }
      return mineAt(serverLevel, pos, state, STATUS_MINING, () -> {
        targetValid = false;
        mineIndex++;
        drillTicks = 0;
      });
    }
    drillTicks = 0;
    return result;
  }

  private int mineAt(ServerLevel serverLevel, BlockPos pos, BlockState state, int workingStatus,
      Runnable advance) {
    int cost = state.canBeReplaced() ? 0 : energyPerBlock();
    if (progress < cost) {
      int draw = Math.min(maxDrawPerTick(), cost - progress);
      int accepted = getEnergyStorage().consumeEnergy(draw, true);
      if (accepted > 0) {
        getEnergyStorage().consumeEnergy(accepted, false);
        getEnergyStorage().updateConsumed(accepted);
        progress += accepted;
        return workingStatus;
      }
      return STATUS_NO_ENERGY;
    }
    List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, serverLevel.getBlockEntity(pos), null,
        MINING_TOOL);
    if (!fitsAll(drops)) {
      return STATUS_FULL;
    }
    serverLevel.destroyBlock(pos, false);
    for (ItemStack drop : drops) {
      insertStacked(inventory, drop.copy());
    }
    progress -= cost;
    advance.run();
    return workingStatus;
  }

  private boolean isMineable(ServerLevel serverLevel, BlockPos pos, BlockState state) {
    if (state.isAir() || state.canBeReplaced()) {
      return false;
    }
    if (!state.getFluidState().isEmpty() && state.getBlock() instanceof LiquidBlock) {
      return false;
    }
    if (state.getDestroySpeed(serverLevel, pos) < 0) {
      return false;
    }
    return state.is(QUARRY_MINEABLE);
  }

  private boolean fitsAll(List<ItemStack> drops) {
    com.faktocraft.common.util.ItemStackHandler temp = new com.faktocraft.common.util.ItemStackHandler(
        inventory.getSlots());
    for (int i = 0; i < inventory.getSlots(); i++) {
      temp.setStackInSlot(i, inventory.getStackInSlot(i).copy());
    }
    for (ItemStack drop : drops) {
      if (!insertStacked(temp, drop.copy()).isEmpty()) {
        return false;
      }
    }
    return true;
  }

  private static ItemStack insertStacked(com.faktocraft.common.util.ItemStackHandler handler, ItemStack stack) {
    for (int i = 0; i < handler.getSlots() && !stack.isEmpty(); i++) {
      if (!handler.getStackInSlot(i).isEmpty()) {
        stack = handler.insertItem(i, stack, false);
      }
    }
    for (int i = 0; i < handler.getSlots() && !stack.isEmpty(); i++) {
      stack = handler.insertItem(i, stack, false);
    }
    return stack;
  }

  private boolean headAtTarget() {
    float dx = headX - (targetX + 0.5F);
    float dy = headY - (targetY + HEAD_HOVER);
    float dz = headZ - (targetZ + 0.5F);
    return dx * dx + dy * dy + dz * dz < 0.05F;
  }

  public boolean headGoal(float[] out) {
    if (stage == STAGE_MINE) {
      if (!targetValid) {
        return false;
      }
      out[0] = targetX + 0.5F;
      out[1] = targetY + HEAD_HOVER;
      out[2] = targetZ + 0.5F;
      return true;
    }
    if (areaSet) {
      out[0] = (minX + maxX + 1) / 2.0F;
      out[1] = worldPosition.getY() + FRAME_HEIGHT_ABOVE - 0.5F;
      out[2] = (minZ + maxZ + 1) / 2.0F;
      return true;
    }
    return false;
  }

  private void moveHead() {
    float[] goal = new float[3];
    if (!headGoal(goal)) {
      return;
    }
    float goalX = goal[0];
    float goalY = goal[1];
    float goalZ = goal[2];
    float dx = goalX - headX;
    float dy = goalY - headY;
    float dz = goalZ - headZ;
    float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    float speed = armSpeed();
    if (distance <= speed) {
      headX = goalX;
      headY = goalY;
      headZ = goalZ;
    } else {
      headX += dx / distance * speed;
      headY += dy / distance * speed;
      headZ += dz / distance * speed;
    }
  }

  @NotNull
  @Override
  public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
    if (cap == ForgeCapabilities.ITEM_HANDLER) {
      return extractOnlyCap.cast();
    }
    return super.getCapability(cap, side);
  }

  @Override
  public void setRemoved() {
    super.setRemoved();
    extractOnlyCap.invalidate();
  }

  @Override
  public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    if (level instanceof ServerLevel serverLevel && areaSet) {
      for (BlockPos framePos : framePositions()) {
        if (serverLevel.getBlockState(framePos).is(QuarryRegistry.QUARRY_FRAME)) {
          serverLevel.removeBlock(framePos, false);
        }
      }
    }
    super.preRemoveSideEffects(pos, state);
  }

  @Override
  public void onBreakServer() {
    super.onBreakServer();
    if (level == null) {
      return;
    }
    for (int i = 0; i < inventory.getSlots(); i++) {
      Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
          worldPosition.getZ() + 0.5, inventory.getStackInSlot(i));
      inventory.setStackInSlot(i, ItemStack.EMPTY);
    }
    for (int i = 0; i < upgrades.getSlots(); i++) {
      Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
          worldPosition.getZ() + 0.5, upgrades.getStackInSlot(i));
      upgrades.setStackInSlot(i, ItemStack.EMPTY);
    }
  }

  @Override
  public net.minecraft.world.phys.AABB getRenderBoundingBox() {
    if (!areaSet || level == null) {
      return new net.minecraft.world.phys.AABB(getBlockPos()).inflate(1);
    }
    return new net.minecraft.world.phys.AABB(minX, level.getMinBuildHeight(), minZ,
        maxX + 1, worldPosition.getY() + FRAME_HEIGHT_ABOVE + 1, maxZ + 1);
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    tag.putBoolean("active", activeState);
    tag.putInt("runMode", runMode);
    tag.putBoolean("areaSet", areaSet);
    tag.putInt("minX", minX);
    tag.putInt("minZ", minZ);
    tag.putInt("maxX", maxX);
    tag.putInt("maxZ", maxZ);
    tag.putInt("stage", stage);
    tag.putInt("clearIndex", clearIndex);
    tag.putInt("frameIndex", frameIndex);
    tag.putLong("mineIndex", mineIndex);
    tag.putInt("progress", progress);
    tag.putBoolean("targetValid", targetValid);
    tag.putInt("targetX", targetX);
    tag.putInt("targetY", targetY);
    tag.putInt("targetZ", targetZ);
    tag.putFloat("headX", headX);
    tag.putFloat("headY", headY);
    tag.putFloat("headZ", headZ);
    CompoundTag inventoryTag = new CompoundTag();
    inventory.save(inventoryTag);
    tag.put("inventory", inventoryTag);
    CompoundTag upgradesTag = new CompoundTag();
    upgrades.save(upgradesTag);
    tag.put("upgrades", upgradesTag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    activeState = tag.getBoolean("active");
    runMode = tag.contains("runMode") ? tag.getInt("runMode") : PipeExtractor.RUN_ALWAYS;
    areaSet = tag.getBoolean("areaSet");
    minX = tag.getInt("minX");
    minZ = tag.getInt("minZ");
    maxX = tag.getInt("maxX");
    maxZ = tag.getInt("maxZ");
    stage = tag.getInt("stage");
    clearIndex = tag.getInt("clearIndex");
    frameIndex = tag.getInt("frameIndex");
    mineIndex = tag.getLong("mineIndex");
    progress = tag.getInt("progress");
    targetValid = tag.getBoolean("targetValid");
    targetX = tag.getInt("targetX");
    targetY = tag.getInt("targetY");
    targetZ = tag.getInt("targetZ");
    if (tag.contains("headX")) {
      headX = tag.getFloat("headX");
      headY = tag.getFloat("headY");
      headZ = tag.getFloat("headZ");
    }
    if (tag.contains("inventory")) {
      inventory.load(tag.getCompound("inventory"));
    }
    if (tag.contains("upgrades")) {
      upgrades.load(tag.getCompound("upgrades"));
    }
  }
}
