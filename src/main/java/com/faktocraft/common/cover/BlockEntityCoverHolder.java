package com.faktocraft.common.cover;

import com.faktocraft.common.registries.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

public class BlockEntityCoverHolder extends BlockEntity implements ICoverHost {

  @Nullable
  private BlockState cover;
  private int coverHoles;

  public BlockEntityCoverHolder(BlockPos pos, BlockState state) {
    super(ModBlockEntities.COVER_HOLDER, pos, state);
  }

  @Nullable
  @Override
  public BlockState getCover() {
    return cover;
  }

  @Override
  public int getCoverHoles() {
    return coverHoles;
  }

  @Override
  public void setCover(@Nullable BlockState cover, int holes) {
    this.cover = cover;
    this.coverHoles = holes;
    CoverSupport.markChanged(this);
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    CoverSupport.save(tag, cover, coverHoles);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    cover = CoverSupport.load(tag);
    coverHoles = CoverSupport.loadHoles(tag);
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

  @Override
  public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
    super.onDataPacket(connection, packet);
    CoverSupport.refreshClientModel(this);
  }

  @Override
  public void onLoad() {
    super.onLoad();
    CoverSupport.onClientLoad(this, this);
  }

  @Override
  public void setRemoved() {
    CoverSupport.onClientRemoved(this, this);
    super.setRemoved();
  }

  @Override
  public ModelData getModelData() {
    return CoverSupport.modelData(cover, coverHoles);
  }
}
