package com.faktocraft.common.block.impl.chunk_loader;

import com.faktocraft.common.config.ModConfig;
import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockEntityChunkLoader extends FaktocraftBlockEntity implements IEnergyBlock {

  public static final int MIN_CHUNKS = 1;
  public static final int DEFAULT_CHUNKS = 4;
  public static final int MAX_CHUNKS = 9;

  public static final int STATUS_OFF = 0;
  public static final int STATUS_ACTIVE = 1;
  public static final int STATUS_NO_ENERGY = 2;
  public static final int STATUS_LIMIT = 3;
  public static final int STATUS_CHARGING = 4;

  private static final int REENGAGE_SECONDS = 30;

  public static final int[][] CHUNK_OFFSETS = {
      { 0, 0 }, { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 },
      { 1, -1 }, { 1, 1 }, { -1, 1 }, { -1, -1 } };

  private boolean enabledByPlayer = false;
  private int chunkCount = DEFAULT_CHUNKS;
  private int status = STATUS_OFF;
  private boolean holdingSlot = false;
  private int forcedCount = 0;
  private boolean ticketsVerified = false;
  private int syncedActiveCount = 0;
  private int syncedMaxActive = 0;
  private int syncedChargePercent = 0;

  public BlockEntityChunkLoader(BlockPos pos, BlockState state) {
    super(ChunkLoaderRegistry.CHUNK_LOADER_BLOCK_ENTITY, pos, state);
    createEnergyStorage(0, ModConfig.server().chunk_loader_energy_capacity, EnergyType.RECEIVE, EnergyTier.HIGH);
  }

  @Override
  public boolean hasBatteryDock() {
    return false;
  }

  @Override
  public boolean supportsRedstoneControl() {
    return false;
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable net.minecraft.core.Direction side) {
    return true;
  }

  public static int clampChunks(int value) {
    return Mth.clamp(value, MIN_CHUNKS, MAX_CHUNKS);
  }

  public int tickCost() {
    return Math.max(1, ModConfig.server().chunk_loader_tick_usage_per_chunk) * chunkCount;
  }

  public boolean isEnabledByPlayer() {
    return enabledByPlayer;
  }

  public void setEnabledByPlayer(boolean value) {
    if (enabledByPlayer == value) {
      return;
    }
    enabledByPlayer = value;
    setChanged();
    updateBlockState();
  }

  public int getChunkCount() {
    return chunkCount;
  }

  public void setChunkCount(int value) {
    int clamped = clampChunks(value);
    if (clamped == chunkCount) {
      return;
    }
    chunkCount = clamped;
    setChanged();
    updateBlockState();
  }

  public int getStatus() {
    return status;
  }

  public void setStatusClient(int value) {
    status = value;
  }

  public int getActiveCount() {
    return syncedActiveCount;
  }

  public void setActiveCountClient(int value) {
    syncedActiveCount = value;
  }

  public int getMaxActive() {
    return syncedMaxActive;
  }

  public void setMaxActiveClient(int value) {
    syncedMaxActive = value;
  }

  public int getChargePercent() {
    if (level != null && level.isClientSide()) {
      return syncedChargePercent;
    }
    long floor = Math.max(1, reengageFloor(tickCost()));
    return (int) Math.min(100, getEnergyStorage().energyStored() * 100L / floor);
  }

  public void setChargePercentClient(int value) {
    syncedChargePercent = value;
  }

  @Override
  public void tickWork(BlockState state) {
    if (!(level instanceof ServerLevel serverLevel)) {
      return;
    }
    ChunkLoaderManager manager = ChunkLoaderManager.get(serverLevel.getServer());
    GlobalPos key = GlobalPos.of(serverLevel.dimension(), worldPosition.immutable());
    getEnergyStorage().updateConsumed(0);

    if (!ticketsVerified) {
      ticketsVerified = true;
      if (forcedCount == 0) {
        forceRange(serverLevel, 0, MAX_CHUNKS, false);
      }
    }

    int newStatus;
    if (!enabledByPlayer) {
      release(serverLevel, manager, key, false);
      newStatus = STATUS_OFF;
    } else {
      if (!holdingSlot) {
        holdingSlot = manager.tryActivate(key);
      }
      if (!holdingSlot) {
        stopForcing(serverLevel, false);
        newStatus = STATUS_LIMIT;
      } else {
        int cost = tickCost();
        boolean affordable = getEnergyStorage().consumeEnergy(cost, true) == cost;
        boolean paying = affordable
            && (forcedCount > 0 || getEnergyStorage().energyStored() >= reengageFloor(cost));
        if (paying) {
          getEnergyStorage().consumeEnergy(cost, false);
          getEnergyStorage().updateConsumed(cost);
          updateForcing(serverLevel);
          newStatus = STATUS_ACTIVE;
        } else {
          stopForcing(serverLevel, false);
          newStatus = affordable ? STATUS_CHARGING : STATUS_NO_ENERGY;
        }
      }
    }

    syncedActiveCount = manager.activeCount();
    syncedMaxActive = ChunkLoaderManager.maxActive();
    if (newStatus != status) {
      status = newStatus;
      updateBlockState();
    }
  }

  private int reengageFloor(int cost) {
    return Math.min(getEnergyStorage().maxEnergy(), cost * 20 * REENGAGE_SECONDS);
  }

  private void forceRange(ServerLevel serverLevel, int from, int to, boolean add) {
    ChunkPos base = ChunkPos.containing(worldPosition);
    for (int i = from; i < to; i++) {
      ChunkLoaderManager.TICKETS.forceChunk(serverLevel, worldPosition,
          base.x() + CHUNK_OFFSETS[i][0], base.z() + CHUNK_OFFSETS[i][1], add, true);
    }
  }

  private void updateForcing(ServerLevel serverLevel) {
    if (forcedCount == chunkCount) {
      return;
    }
    if (forcedCount == 0) {

      forceRange(serverLevel, 0, MAX_CHUNKS, false);
      forceRange(serverLevel, 0, chunkCount, true);
    } else if (forcedCount < chunkCount) {
      forceRange(serverLevel, forcedCount, chunkCount, true);
    } else {
      forceRange(serverLevel, chunkCount, forcedCount, false);
    }
    forcedCount = chunkCount;
  }

  private void stopForcing(ServerLevel serverLevel, boolean thorough) {
    if (forcedCount > 0 || thorough) {
      forceRange(serverLevel, 0, MAX_CHUNKS, false);
      forcedCount = 0;
    }
  }

  private void release(ServerLevel serverLevel, ChunkLoaderManager manager, GlobalPos key, boolean thorough) {
    stopForcing(serverLevel, thorough);
    if (holdingSlot || thorough) {
      manager.deactivate(key);
      holdingSlot = false;
    }
  }

  @Override
  public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    if (level instanceof ServerLevel serverLevel) {
      release(serverLevel, ChunkLoaderManager.get(serverLevel.getServer()),
          GlobalPos.of(serverLevel.dimension(), worldPosition.immutable()), true);
    }
    super.preRemoveSideEffects(pos, state);
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    tag.putBoolean("enabled", enabledByPlayer);
    tag.putInt("chunkCount", chunkCount);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    enabledByPlayer = tag.getBooleanOr("enabled", false);
    if (tag.contains("chunkCount")) {
      chunkCount = clampChunks(tag.getIntOr("chunkCount", 0));
    }
  }
}
