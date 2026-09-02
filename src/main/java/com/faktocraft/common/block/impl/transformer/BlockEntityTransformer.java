package com.faktocraft.common.block.impl.transformer;

import com.faktocraft.common.energy.interfaces.IEnergyBlock;
import com.faktocraft.common.entity.block.FaktocraftBlockEntity;
import com.faktocraft.common.enums.EnergyTier;
import com.faktocraft.common.enums.EnergyType;
import com.faktocraft.common.enums.TransformerMode;
import com.faktocraft.common.interfaces.block.IStateFacing;
import com.faktocraft.common.interfaces.entity.IMachineActions;
import com.faktocraft.common.interfaces.entity.ITransformer;
import com.faktocraft.common.network.ModNetworking;
import com.faktocraft.common.network.packet.PacketTransformerMode;
import com.faktocraft.common.registries.machines.M1Registry;
import com.faktocraft.common.tier.TransformerTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockEntityTransformer extends FaktocraftBlockEntity
    implements IEnergyBlock, ITransformer, IMachineActions.ITransformerActions {

  private final TransformerTier tier;
  private TransformerMode transformerMode = TransformerMode.STEP_UP;

  public BlockEntityTransformer(BlockPos pos, BlockState state) {
    super(M1Registry.TRANSFORMER_BE, pos, state);

    BlockTransformer block = (BlockTransformer) state.getBlock();
    tier = block.getTransformerTier();
    createEnergyStorage(0, tier.getMaxTier().getBasicTransfer(), EnergyType.TRANSFORMER, tier.getMaxTier());

    setRedstoneOnly(true);
  }

  @Override
  public boolean showBarInGui() {
    return false;
  }

  @Override
  public boolean canExtractEnergyDir(@Nullable Direction side) {
    if (isRedstoneBlocked()) {
      return false;
    }
    if (side == null) {
      return true;
    }
    IStateFacing blockFacing = (IStateFacing) getBlockState().getBlock();
    Direction facingDirection = blockFacing.getDirection(getBlockState());
    return (transformerMode == TransformerMode.STEP_UP) == (facingDirection == side);
  }

  @Override
  public boolean canReceiveEnergyDir(@Nullable Direction side) {
    if (isRedstoneBlocked()) {
      return false;
    }
    if (side == null) {
      return true;
    }
    IStateFacing blockFacing = (IStateFacing) getBlockState().getBlock();
    Direction facingDirection = blockFacing.getDirection(getBlockState());
    return (transformerMode == TransformerMode.STEP_UP) == (facingDirection != side);
  }

  @Override
  public boolean showVertical() {
    return false;
  }

  @Override
  public int customEnergyExtractTick() {
    return energyExtractTier().getBasicTransfer();
  }

  @Override
  public int customEnergyReceiveTick() {
    return energyReceiveTier().getBasicTransfer();
  }

  @Override
  protected void saveAdditional(CompoundTag tag) {
    tag.putInt("transformerMode", transformerMode.getId());
    super.saveAdditional(tag);
  }

  @Override
  public void load(CompoundTag tag) {
    super.load(tag);
    transformerMode = TransformerMode.getModeFromId(
        tag.contains("transformerMode") ? tag.getInt("transformerMode") : TransformerMode.STEP_UP.getId());
  }

  public Runnable changeMode() {
    BlockPos pos = getBlockPos();
    return () -> ModNetworking.sendToServer(new PacketTransformerMode(pos));
  }

  @Override
  public void updateMode() {
    switch (transformerMode) {
      case STEP_UP -> transformerMode = TransformerMode.STEP_DOWN;
      case STEP_DOWN -> transformerMode = TransformerMode.STEP_UP;
    }

    updateBlockState();
  }

  @Override
  public EnergyTier energyExtractTier() {
    return transformerMode == TransformerMode.STEP_UP ? tier.getMaxTier() : tier.getMinTier();
  }

  @Override
  public EnergyTier energyReceiveTier() {
    return transformerMode == TransformerMode.STEP_UP ? tier.getMinTier() : tier.getMaxTier();
  }

  public TransformerTier getTransformerTier() {
    return tier;
  }

  public TransformerMode getTransformerMode() {
    return transformerMode;
  }
}
