package com.faktocraft.common.block.impl.cable;

import com.faktocraft.common.util.LegacyNbtBlockEntity;
import com.faktocraft.common.block.ISupportHost;
import com.faktocraft.common.energy.interfaces.IEnergyTransmitter;
import com.faktocraft.common.energy.provider.EnergyCore;
import com.faktocraft.common.energy.provider.EnergyNetwork;
import com.faktocraft.common.registries.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockEntityCable extends LegacyNbtBlockEntity
    implements IEnergyTransmitter, ISupportHost, com.faktocraft.common.cover.ICoverHost {

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
  public net.neoforged.neoforge.model.data.ModelData getModelData() {
    return com.faktocraft.common.cover.CoverSupport.modelData(cover, coverHoles);
  }

  @Override
  public void onDataPacket(net.minecraft.network.Connection connection,
      net.minecraft.world.level.storage.ValueInput input) {
    BlockState previousCover = cover;
    int previousHoles = coverHoles;
    super.onDataPacket(connection, input);
    if (cover != previousCover || coverHoles != previousHoles) {
      com.faktocraft.common.cover.CoverSupport.refreshClientModel(this);
    }
  }

  @Override
  public void onLoad() {
    super.onLoad();
    com.faktocraft.common.cover.CoverSupport.onClientLoad(this, this);
    if (level != null && !level.isClientSide() && getNetwork() == null) {
      EnergyCore.get(level).getNetworks().scheduleAdopt(worldPosition);
    }
  }

  @Override
  public void setRemoved() {
    com.faktocraft.common.cover.CoverSupport.onClientRemoved(this, this);
    super.setRemoved();
  }

  @Override
  protected void saveAdditional(net.minecraft.nbt.CompoundTag tag) {
    super.saveAdditional(tag);
    com.faktocraft.common.cover.CoverSupport.save(tag, cover, coverHoles);
  }

  @Override
  public void load(net.minecraft.nbt.CompoundTag tag) {
    super.load(tag);
    cover = com.faktocraft.common.cover.CoverSupport.load(tag);
    coverHoles = com.faktocraft.common.cover.CoverSupport.loadHoles(tag);
  }

  @Override
  public net.minecraft.nbt.CompoundTag getUpdateTag() {
    net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
    saveAdditional(tag);
    return tag;
  }

  @Nullable
  @Override
  public net.minecraft.network.protocol.Packet<
      net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
    return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
  }

  public BlockEntityCable(BlockPos pos, BlockState state) {
    super(ModBlockEntities.CABLE, pos, state);
  }

  protected BlockEntityCable(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos,
      BlockState state) {
    super(type, pos, state);
  }

  @Nullable
  @Override
  public EnergyNetwork getNetwork() {
    if (level == null) {
      return null;
    }
    return EnergyCore.get(level).getNetworks().getNetwork(getBlockPos());
  }

  private net.minecraft.core.Direction supportDirection;

  private long supportCheckedAt = -SUPPORT_REFRESH_TICKS;

  @Override
  @org.jetbrains.annotations.Nullable
  public net.minecraft.core.Direction supportDirection() {
    if (level == null) {
      return null;
    }
    long now = level.getGameTime();
    if (now - supportCheckedAt >= SUPPORT_REFRESH_TICKS) {
      supportCheckedAt = now - Math.floorMod(worldPosition.hashCode(), SUPPORT_REFRESH_TICKS);
      supportDirection = com.faktocraft.common.block.PipeSupport.directionFor(level, worldPosition, getBlockState());
    }
    return supportDirection;
  }

}
