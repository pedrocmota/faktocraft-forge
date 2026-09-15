package com.faktocraft.common.block.impl.monitor;

import com.faktocraft.client.render.StatusClientBridges;
import com.faktocraft.common.config.ModConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class BlockEntityStatusMonitor extends BlockEntity {

  public static final int STATUS_NONE = 0;
  public static final int STATUS_OK = 1;
  public static final int STATUS_UNLOADED = 2;
  public static final int STATUS_MISSING = 3;

  private static final String TAG_DIMENSION = "targetDim";
  private static final String TAG_POS = "targetPos";
  private static final String TAG_DATA = "data";
  private static final String TAG_STATUS = "status";

  @Nullable
  private ResourceKey<Level> targetDimension;
  @Nullable
  private BlockPos target;
  private CompoundTag data = new CompoundTag();
  private int status = STATUS_NONE;
  private int timer;

  private int dataVersion;
  private int cachedVersion = -1;
  @Nullable
  private List<StatusLine> cachedLines;
  @Nullable
  private BlockState cachedState;
  @Nullable
  private Object cachedBridge;

  public BlockEntityStatusMonitor(BlockPos pos, BlockState state) {
    super(MonitorRegistry.STATUS_MONITOR_BLOCK_ENTITY, pos, state);
  }

  @Nullable
  public BlockPos target() {
    return target;
  }

  @Nullable
  public ResourceKey<Level> targetDimension() {
    return targetDimension;
  }

  public boolean hasTarget() {
    return target != null && targetDimension != null;
  }

  public int status() {
    return status;
  }

  public CompoundTag data() {
    return data;
  }

  public int dataVersion() {
    return dataVersion;
  }

  public void setTarget(ResourceKey<Level> dimension, BlockPos pos) {
    targetDimension = dimension;
    target = pos.immutable();
    data = new CompoundTag();
    status = STATUS_UNLOADED;
    timer = 0;
    sync();
  }

  public void clearTarget() {
    targetDimension = null;
    target = null;
    data = new CompoundTag();
    status = STATUS_NONE;
    sync();
  }

  public Component describeTarget() {
    if (!hasTarget()) {
      return Component.translatable("gui.faktocraft.status_monitor.no_target").withStyle(ChatFormatting.GRAY);
    }
    return Component.translatable("gui.faktocraft.status_monitor.target", targetName(),
        target.getX() + ", " + target.getY() + ", " + target.getZ());
  }

  public Component targetName() {
    if (level == null || !data.contains(StatusSources.TAG_STATE)) {
      return Component.literal("?");
    }
    return StatusSources.readState(level, data).getBlock().getName();
  }

  public void serverTick() {
    if (level == null || level.isClientSide()) {
      return;
    }
    int interval = Math.max(1, ModConfig.server().status_monitor_refresh_ticks);
    if (timer++ % interval != 0) {
      return;
    }
    int newStatus;
    CompoundTag newData = data;
    if (!hasTarget()) {
      newStatus = STATUS_NONE;
      newData = new CompoundTag();
    } else {
      MinecraftServer server = level.getServer();
      ServerLevel targetLevel = server != null ? server.getLevel(targetDimension) : null;
      if (targetLevel == null || !targetLevel.isLoaded(target)) {
        newStatus = STATUS_UNLOADED;
      } else if (targetLevel.getBlockState(target).isAir()) {
        newStatus = STATUS_MISSING;
      } else {
        newStatus = STATUS_OK;
        newData = StatusSources.collect(targetLevel, target);
      }
    }
    if (newStatus != status || !newData.equals(data)) {
      status = newStatus;
      data = newData;
      sync();
    }
  }

  private void sync() {
    setChanged();
    if (level != null && !level.isClientSide()) {
      level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
  }

  @Override
  public AABB getRenderBoundingBox() {
    BlockState state = getBlockState();
    if (!(state.getBlock() instanceof BlockStatusMonitor)) {
      return super.getRenderBoundingBox();
    }
    Direction right = BlockStatusMonitor.rightOf(BlockStatusMonitor.facingOf(state));
    BlockPos far = worldPosition.relative(right, BlockStatusMonitor.WIDTH - 1).above(BlockStatusMonitor.HEIGHT - 1);
    return new AABB(worldPosition).minmax(new AABB(far));
  }

  public boolean joinedRight() {
    return joined(BlockStatusMonitor.WIDTH);
  }

  public boolean joinedLeft() {
    return joined(-BlockStatusMonitor.WIDTH);
  }

  private boolean joined(int offset) {
    if (level == null) {
      return false;
    }
    BlockState state = getBlockState();
    if (!(state.getBlock() instanceof BlockStatusMonitor)) {
      return false;
    }
    BlockPos other = worldPosition.relative(BlockStatusMonitor.rightOf(BlockStatusMonitor.facingOf(state)), offset);
    BlockState otherState = level.getBlockState(other);
    return otherState.getBlock() instanceof BlockStatusMonitor && BlockStatusMonitor.isMaster(otherState)
        && BlockStatusMonitor.facingOf(otherState) == BlockStatusMonitor.facingOf(state);
  }

  public BlockState targetState() {
    refreshCache();
    return cachedState != null ? cachedState : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
  }

  public List<StatusLine> lines() {
    refreshCache();
    return cachedLines != null ? cachedLines : List.of();
  }

  @Nullable
  public Object bridge() {
    refreshCache();
    return cachedBridge;
  }

  private void refreshCache() {
    if (level == null || cachedVersion == dataVersion) {
      return;
    }
    cachedVersion = dataVersion;
    cachedState = StatusSources.readState(level, data);
    cachedLines = StatusSources.lines(cachedState, data);
    cachedBridge = null;
    if (status == STATUS_OK && target != null && !StatusClientBridges.isEmpty()
        && level.dimension().equals(targetDimension) && level.isLoaded(target)) {
      BlockState liveState = level.getBlockState(target);
      if (!liveState.isAir()) {
        cachedBridge = StatusClientBridges.build(level, target, liveState, level.getBlockEntity(target), data);
      }
    }
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    if (targetDimension != null && target != null) {
      tag.putString(TAG_DIMENSION, targetDimension.location().toString());
      tag.putLong(TAG_POS, target.asLong());
    }
    tag.put(TAG_DATA, data.copy());
    tag.putInt(TAG_STATUS, status);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    if (tag.contains(TAG_DIMENSION) && tag.contains(TAG_POS)) {
      ResourceLocation id = ResourceLocation.tryParse(tag.getString(TAG_DIMENSION));
      targetDimension = id != null ? ResourceKey.create(Registries.DIMENSION, id) : null;
      target = targetDimension != null ? BlockPos.of(tag.getLong(TAG_POS)) : null;
    } else {
      targetDimension = null;
      target = null;
    }
    data = tag.getCompound(TAG_DATA).copy();
    status = tag.getInt(TAG_STATUS);
    dataVersion++;
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
}
