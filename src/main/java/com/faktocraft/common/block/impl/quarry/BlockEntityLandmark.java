package com.faktocraft.common.block.impl.quarry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class BlockEntityLandmark extends BlockEntity {

  public long nextScanTime = Long.MIN_VALUE;

  @Nullable
  public BlockPos eastPartner;

  @Nullable
  public BlockPos southPartner;

  public BlockEntityLandmark(BlockPos pos, BlockState state) {
    super(QuarryRegistry.LANDMARK_BLOCK_ENTITY, pos, state);
  }

  @Override
  public AABB getRenderBoundingBox() {
    return new AABB(worldPosition).expandTowards(BlockLandmark.MAX_SPAN, 0, BlockLandmark.MAX_SPAN).inflate(1);
  }
}
