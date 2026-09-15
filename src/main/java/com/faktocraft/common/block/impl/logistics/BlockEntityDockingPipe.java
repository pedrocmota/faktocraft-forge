package com.faktocraft.common.block.impl.logistics;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class BlockEntityDockingPipe extends BlockEntity implements com.faktocraft.common.cover.ICoverHost {

  @org.jetbrains.annotations.Nullable
  private BlockState cover;
  private int coverHoles;

  @org.jetbrains.annotations.Nullable
  @Override
  public BlockState getCover() {
    return cover;
  }

  @Override
  public int getCoverHoles() {
    return coverHoles;
  }

  @Override
  public void setCover(@org.jetbrains.annotations.Nullable BlockState cover, int holes) {
    this.cover = cover;
    this.coverHoles = holes;
    com.faktocraft.common.cover.CoverSupport.markChanged(this);
  }

  @Override
  public net.minecraftforge.client.model.data.ModelData getModelData() {
    return com.faktocraft.common.cover.CoverSupport.modelData(cover, coverHoles);
  }

  @Override
  public void onDataPacket(net.minecraft.network.Connection connection,
      net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket packet) {
    BlockState previousCover = cover;
    int previousHoles = coverHoles;
    super.onDataPacket(connection, packet);
    if (previousCover != cover || previousHoles != coverHoles) {
      com.faktocraft.common.cover.CoverSupport.refreshClientModel(this);
    }
  }

  @Override
  public void onLoad() {
    super.onLoad();
    com.faktocraft.common.cover.CoverSupport.onClientLoad(this, this);
    if (level != null && !level.isClientSide()) {
      LogisticsCores.markDirtyNear(level, worldPosition);
    }
  }

  @Override
  public void setRemoved() {
    com.faktocraft.common.cover.CoverSupport.onClientRemoved(this, this);
    super.setRemoved();
  }

  @Nullable
  private Direction selectedInventory;
  private int timeoutTicks = -1;

  protected BlockEntityDockingPipe(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  protected abstract boolean canDockTo(BlockState state);

  public int timeoutTicks() {
    return timeoutTicks > 0 ? timeoutTicks : Math.max(20, ModConfig.server().logistics_machine_timeout);
  }

  public void setTimeoutTicks(int ticks) {
    this.timeoutTicks = Math.max(20, Math.min(72000, ticks));
    setChanged();
    sync();
  }

  public List<Direction> inventoryDirections() {
    List<Direction> result = new ArrayList<>();
    if (level == null) {
      return result;
    }
    for (Direction direction : BlockChassis.inventoryDirections(level, worldPosition)) {
      if (canDockTo(level.getBlockState(worldPosition.relative(direction)))) {
        result.add(direction);
      }
    }
    return result;
  }

  @Nullable
  public Direction selectedInventoryDirection() {
    List<Direction> candidates = inventoryDirections();
    if (candidates.isEmpty()) {
      return null;
    }
    if (selectedInventory != null && candidates.contains(selectedInventory)) {
      return selectedInventory;
    }
    Direction first = candidates.get(0);
    if (level != null && !level.isClientSide()) {
      selectedInventory = first;
      setChanged();
    }
    return first;
  }

  @Nullable
  public BlockPos dockedPos() {
    Direction direction = selectedInventoryDirection();
    return direction != null ? worldPosition.relative(direction) : null;
  }

  @Nullable
  public Direction dockedSide() {
    Direction direction = selectedInventoryDirection();
    return direction != null ? direction.getOpposite() : null;
  }

  public boolean cycleInventory(@Nullable Player player) {
    if (level == null || level.isClientSide()) {
      return false;
    }
    List<Direction> candidates = inventoryDirections();
    if (candidates.size() < 2) {
      if (player != null) {
        player.displayClientMessage(Component.translatable("logistics." + Faktocraft.MODID
            + (candidates.isEmpty() ? ".chassis.inventory_none" : ".chassis.inventory_single")), true);
      }
      return false;
    }
    Direction current = selectedInventoryDirection();
    selectedInventory = candidates.get((candidates.indexOf(current) + 1) % candidates.size());
    setChanged();
    refreshConnections();
    LogisticsCores.markDirtyNear(level, worldPosition);
    if (player != null) {
      BlockPos target = worldPosition.relative(selectedInventory);
      player.displayClientMessage(Component.translatable("logistics." + Faktocraft.MODID
          + ".chassis.inventory_selected",
          level.getBlockState(target).getBlock().getName(), selectedInventory.getName()), true);
    }
    return true;
  }

  private void refreshConnections() {
    if (level == null || !(getBlockState().getBlock() instanceof BlockDockingPipe block)) {
      return;
    }
    BlockState state = block.withConnections(getBlockState(), level, worldPosition);
    level.setBlock(worldPosition, state, 3);
    level.sendBlockUpdated(worldPosition, state, state, 3);
  }

  protected void sync() {
    if (level != null && !level.isClientSide()) {
      level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    tag.putInt("timeout", timeoutTicks);
    if (selectedInventory != null) {
      tag.putInt("invDir", selectedInventory.get3DDataValue());
    }
    com.faktocraft.common.cover.CoverSupport.save(tag, cover, coverHoles);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    timeoutTicks = tag.contains("timeout") ? tag.getInt("timeout") : -1;
    selectedInventory = tag.contains("invDir") ? Direction.from3DDataValue(tag.getInt("invDir")) : null;
    cover = com.faktocraft.common.cover.CoverSupport.load(tag);
    coverHoles = com.faktocraft.common.cover.CoverSupport.loadHoles(tag);
  }

  public CompoundTag copyConfig() {
    CompoundTag tag = saveWithoutMetadata();
    tag.remove("invDir");
    tag.remove("cover");
    tag.remove("coverHoles");
    return tag;
  }

  public void pasteConfig(CompoundTag config) {
    Direction dock = selectedInventory;
    BlockState keptCover = cover;
    int keptHoles = coverHoles;
    load(config.copy());
    selectedInventory = dock;
    cover = keptCover;
    coverHoles = keptHoles;
    setChanged();
    if (level != null) {
      level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
  }

  @Override
  public CompoundTag getUpdateTag() {
    CompoundTag tag = new CompoundTag();
    saveAdditional(tag);
    return tag;
  }

  @Override
  public Packet<ClientGamePacketListener> getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }
}
