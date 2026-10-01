package com.faktocraft.common.block.impl.cable;

import com.faktocraft.common.enums.EnergyTier;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockEntityBreaker extends BlockEntityCable {

  @Nullable
  private EnergyTier adoptedTier;

  public float handleAngle = 0.0F;
  public double handleLastTime = Double.NaN;

  public BlockEntityBreaker(BlockPos pos, BlockState state) {
    super(com.faktocraft.common.registries.ModBlockEntities.BREAKER, pos, state);
  }

  @Nullable
  public EnergyTier getAdoptedTier() {
    return adoptedTier;
  }

  public void setAdoptedTier(@Nullable EnergyTier tier) {
    this.adoptedTier = tier;
    setChanged();
  }

  private boolean redstoneOnly = false;

  public boolean isRedstoneOnly() {
    return redstoneOnly;
  }

  public void setRedstoneOnly(boolean redstoneOnly) {
    this.redstoneOnly = redstoneOnly;
    setChanged();
    if (level != null && !level.isClientSide()) {
      level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    tag.putInt("adoptedTier", adoptedTier != null ? adoptedTier.getLvl() : -1);
    tag.putBoolean("redstoneOnly", redstoneOnly);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    int lvl = tag.contains("adoptedTier") ? tag.getIntOr("adoptedTier", 0) : -1;
    adoptedTier = lvl >= 0 ? EnergyTier.getTierFromLvl(lvl) : null;
    redstoneOnly = tag.getBooleanOr("redstoneOnly", false);
  }

  @Override
  public CompoundTag getUpdateTag() {
    CompoundTag tag = new CompoundTag();
    saveAdditional(tag);
    return tag;
  }

  @Nullable
  @Override
  public net.minecraft.network.protocol.Packet<
      net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
    return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
  }
}
