package com.faktocraft.common.block.impl.machines.geo_scanner;

import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.item.impl.tools.Prospector;
import com.faktocraft.common.scan.ScanChannel;
import com.faktocraft.common.scan.ScanChannels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockEntityGeoScanner extends FaktocraftBlockEntity implements IEnergyBlock {

  public static final int RADIUS = 14;
  public static final int TOTAL_CHUNKS = countInRange();
  public static final int SCAN_COST = 25000;
  public static final int SCAN_DURATION_TICKS = Prospector.SCAN_DURATION_TICKS;
  public static final int ENERGY_CAPACITY = 100000;

  private int code = ScanChannels.DEFAULT_CODE;
  private int lastChannelRevision = -1;
  @Nullable
  private CompoundTag legacyScans;
  private boolean running = false;
  private boolean jobActive = false;
  private int jobCx;
  private int jobCz;
  private int jobRemaining;
  private boolean manualPending = false;
  private int manualCx;
  private int manualCz;
  private int revision = 0;

  public BlockEntityGeoScanner(BlockPos pos, BlockState state) {
    super(GeoScannerRegistry.GEO_SCANNER_BLOCK_ENTITY, pos, state);
    createEnergyStorage(0, ENERGY_CAPACITY, EnergyType.RECEIVE, EnergyTier.HIGH);
  }

  @Override
  public boolean hasBatteryDock() {
    return false;
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable Direction side) {
    return true;
  }

  public static boolean inRange(int dx, int dz) {
    return dx * dx + dz * dz <= RADIUS * RADIUS;
  }

  private static int countInRange() {
    int count = 0;
    for (int dx = -RADIUS; dx <= RADIUS; dx++) {
      for (int dz = -RADIUS; dz <= RADIUS; dz++) {
        if (inRange(dx, dz)) {
          count++;
        }
      }
    }
    return count;
  }

  public static String scanKey(int chunkX, int chunkZ) {
    return ScanChannel.localKey(chunkX, chunkZ);
  }

  public ChunkPos centerChunk() {
    return new ChunkPos(getBlockPos());
  }

  public int getCode() {
    return code;
  }

  public String codeText() {
    return ScanChannels.codeText(code);
  }

  public void setCode(int newCode) {
    int accepted = ScanChannels.sanitize(newCode);
    if (accepted == code) {
      return;
    }
    code = accepted;
    lastChannelRevision = -1;
    revision++;
    setChanged();
    updateBlockState();
  }

  @Nullable
  private ScanChannel channel() {
    if (!(level instanceof ServerLevel serverLevel)) {
      return null;
    }
    return ScanChannels.get(serverLevel).channel(code);
  }

  public boolean hasScan(int chunkX, int chunkZ) {
    ScanChannel channel = channel();
    return channel != null && level != null && channel.has(level, chunkX, chunkZ);
  }

  public CompoundTag collectScans() {
    ScanChannel channel = channel();
    if (channel == null || level == null) {
      return new CompoundTag();
    }
    ChunkPos center = centerChunk();
    return channel.collect(level, center.x, center.z, RADIUS);
  }

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean value) {
    if (running != value) {
      running = value;
      revision++;
      setChanged();
      updateBlockState();
    }
  }

  public boolean isJobActive() {
    return jobActive;
  }

  public int getJobCx() {
    return jobCx;
  }

  public int getJobCz() {
    return jobCz;
  }

  public int getJobRemaining() {
    return jobRemaining;
  }

  public boolean isManualPending() {
    return manualPending;
  }

  public int getManualCx() {
    return manualCx;
  }

  public int getManualCz() {
    return manualCz;
  }

  public void setManualTarget(int cx, int cz) {
    ChunkPos center = centerChunk();
    if (!inRange(cx - center.x, cz - center.z)
        || hasScan(cx, cz)
        || (jobActive && jobCx == cx && jobCz == cz)) {
      return;
    }
    manualPending = true;
    manualCx = cx;
    manualCz = cz;
    revision++;
    setChanged();
    updateBlockState();
  }

  public int getRevision() {
    return revision;
  }

  public int getScannedCount() {
    ScanChannel channel = channel();
    if (channel == null || level == null) {
      return 0;
    }
    ChunkPos center = centerChunk();
    int count = 0;
    for (int dx = -RADIUS; dx <= RADIUS; dx++) {
      for (int dz = -RADIUS; dz <= RADIUS; dz++) {
        if (inRange(dx, dz) && channel.has(level, center.x + dx, center.z + dz)) {
          count++;
        }
      }
    }
    return count;
  }

  @Override
  public void tickWork(BlockState state) {
    if (level == null || level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
      return;
    }
    ScanChannel channel = ScanChannels.get(serverLevel).channel(code);
    if (legacyScans != null) {
      channel.importLocal(serverLevel, legacyScans);
      legacyScans = null;
      setChanged();
    }
    if (channel.revision() != lastChannelRevision) {
      lastChannelRevision = channel.revision();
      revision++;
      updateBlockState();
    }
    if (jobActive) {
      setActive(true);
      if (--jobRemaining <= 0) {
        CompoundTag entries = Prospector.computeScanEntries(serverLevel, jobCx, jobCz);
        CompoundTag scan = new CompoundTag();
        scan.putLong("t", serverLevel.getGameTime());
        scan.put("entries", entries);
        channel.put(serverLevel, jobCx, jobCz, scan);
        lastChannelRevision = channel.revision();
        jobActive = false;
        revision++;
        setChanged();
        updateBlockState();
        notifyRareFinds(serverLevel, entries);
      }
      return;
    }
    int[] next = null;
    if (manualPending) {
      if (channel.has(serverLevel, manualCx, manualCz)) {
        manualPending = false;
        revision++;
        setChanged();
      } else {
        next = new int[] { manualCx, manualCz };
      }
    }
    if (next == null) {
      if (!running) {
        setActive(false);
        return;
      }
      next = nextUnscanned(channel);
      if (next == null) {
        running = false;
        revision++;
        setActive(false);
        setChanged();
        updateBlockState();
        return;
      }
    }
    if (getEnergyStorage().consumeEnergy(SCAN_COST, true) != SCAN_COST) {
      setActive(false);
      return;
    }
    getEnergyStorage().consumeEnergy(SCAN_COST, false);
    getEnergyStorage().updateConsumed(SCAN_COST);
    if (manualPending && next[0] == manualCx && next[1] == manualCz) {
      manualPending = false;
    }
    jobActive = true;
    jobCx = next[0];
    jobCz = next[1];
    jobRemaining = SCAN_DURATION_TICKS;
    revision++;
    setActive(true);
    setChanged();
    updateBlockState();
  }

  private void notifyRareFinds(ServerLevel serverLevel, CompoundTag entries) {
    boolean iridium = false;
    boolean giantOil = false;
    for (String id : entries.getAllKeys()) {
      iridium |= id.contains("iridium");
      giantOil |= id.equals("faktocraft:oil_giant");
    }
    if (!iridium && !giantOil) {
      return;
    }
    java.util.List<net.minecraft.network.chat.Component> names = new java.util.ArrayList<>();
    if (iridium) {
      names.add(net.minecraft.network.chat.Component.translatable("item.faktocraft.iridium"));
    }
    if (giantOil) {
      names.add(net.minecraft.network.chat.Component.translatable("gui.faktocraft.prospector.oil_giant"));
    }
    for (net.minecraft.server.level.ServerPlayer player : serverLevel.players()) {
      if (player.blockPosition().distSqr(getBlockPos()) > 64 * 64) {
        continue;
      }
      for (net.minecraft.network.chat.Component name : names) {
        player.sendSystemMessage(net.minecraft.network.chat.Component
            .translatable("chat.faktocraft.geo_scanner.rare_found", name, jobCx, jobCz)
            .withStyle(net.minecraft.ChatFormatting.GOLD));
      }
    }
  }

  @Nullable
  private int[] nextUnscanned(ScanChannel channel) {
    ChunkPos center = centerChunk();
    for (int r = 0; r <= RADIUS; r++) {
      for (int dx = -r; dx <= r; dx++) {
        for (int dz = -r; dz <= r; dz++) {
          if (Math.max(Math.abs(dx), Math.abs(dz)) != r || !inRange(dx, dz)) {
            continue;
          }
          int cx = center.x + dx;
          int cz = center.z + dz;
          if (!channel.has(level, cx, cz)) {
            return new int[] { cx, cz };
          }
        }
      }
    }
    return null;
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    tag.putInt("code", code);
    if (legacyScans != null) {
      tag.put("scans", legacyScans.copy());
    }
    tag.putBoolean("running", running);
    tag.putBoolean("jobActive", jobActive);
    tag.putInt("jobCx", jobCx);
    tag.putInt("jobCz", jobCz);
    tag.putInt("jobRemaining", jobRemaining);
    tag.putBoolean("manualPending", manualPending);
    tag.putInt("manualCx", manualCx);
    tag.putInt("manualCz", manualCz);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    code = tag.contains("code") ? ScanChannels.sanitize(tag.getInt("code")) : ScanChannels.DEFAULT_CODE;
    legacyScans = tag.contains("scans") && !tag.getCompound("scans").isEmpty() ? tag.getCompound("scans").copy()
        : null;
    running = tag.getBoolean("running");
    jobActive = tag.getBoolean("jobActive");
    jobCx = tag.getInt("jobCx");
    jobCz = tag.getInt("jobCz");
    jobRemaining = tag.getInt("jobRemaining");
    manualPending = tag.getBoolean("manualPending");
    manualCx = tag.getInt("manualCx");
    manualCz = tag.getInt("manualCz");
  }
}
