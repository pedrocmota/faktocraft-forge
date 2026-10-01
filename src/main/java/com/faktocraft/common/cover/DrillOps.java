package com.faktocraft.common.cover;

import net.minecraft.core.registries.Registries;
import com.faktocraft.common.block.VoxelBlock;
import com.faktocraft.common.registries.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class DrillOps {
  public static final TagKey<Block> UNDRILLABLE = TagKey.create(Registries.BLOCK,
      Identifier.fromNamespaceAndPath("faktocraft", "undrillable"));

  private DrillOps() {
  }

  public static boolean isBored(BlockState state) {
    return state.is(ModBlocks.DRILLED_BLOCK) || CoverSupport.isCovered(state);
  }

  @Nullable
  private static ICoverHost host(Level level, BlockPos pos) {
    return level.getBlockEntity(pos) instanceof ICoverHost host && host.getCover() != null ? host : null;
  }

  public static int holes(Level level, BlockPos pos, BlockState state) {
    if (state.is(ModBlocks.DRILLED_BLOCK)) {
      return CoverSupport.connectionMask(state);
    }
    ICoverHost host = host(level, pos);
    return host != null ? host.getCoverHoles() : 0;
  }

  public static boolean canDrill(Level level, BlockPos pos, BlockState state, @Nullable Player player,
      Direction face) {
    if (state.is(ModBlocks.DRILLED_BLOCK)) {
      return !CoverSupport.hasHole(CoverSupport.connectionMask(state), face);
    }
    if (CoverSupport.isCovered(state)) {
      ICoverHost host = host(level, pos);
      return host != null && !CoverSupport.hasHole(host.getCoverHoles(), face);
    }
    if (state.isAir() || !state.getFluidState().isEmpty() || state.hasBlockEntity()
        || state.getRenderShape() != RenderShape.MODEL || state.is(UNDRILLABLE)
        || state.getBlock() instanceof VoxelBlock) {
      return false;
    }
    if (!state.isCollisionShapeFullBlock(level, pos) || state.getDestroySpeed(level, pos) < 0) {
      return false;
    }
    if (player != null && player.isCreative()) {
      return true;
    }
    return !state.requiresCorrectToolForDrops() || !state.is(BlockTags.INCORRECT_FOR_IRON_TOOL);
  }

  @Nullable
  public static Direction drillFace(Level level, BlockPos pos, BlockState state, @Nullable Player player,
      Direction face) {
    if (canDrill(level, pos, state, player, face)) {
      return face;
    }
    if (isBored(state) && canDrill(level, pos, state, player, face.getOpposite())) {
      return face.getOpposite();
    }
    return null;
  }

  public static boolean drill(Level level, BlockPos pos, Direction face) {
    BlockState state = level.getBlockState(pos);
    if (level.isClientSide() || !canDrill(level, pos, state, null, face)) {
      return false;
    }
    int bit = CoverSupport.bit(face);
    if (state.is(ModBlocks.DRILLED_BLOCK)) {
      int holes = CoverSupport.connectionMask(state) | bit;
      ICoverHost host = host(level, pos);
      BlockState cover = host != null ? host.getCover() : null;
      level.setBlock(pos, CoverSupport.drilledState(holes), 3);
      if (level.getBlockEntity(pos) instanceof ICoverHost updated) {
        updated.setCover(cover, holes);
      }
      return true;
    }
    if (CoverSupport.isCovered(state)) {
      ICoverHost host = host(level, pos);
      if (host == null) {
        return false;
      }
      host.setCover(host.getCover(), host.getCoverHoles() | bit);
      return true;
    }
    level.setBlock(pos, CoverSupport.drilledState(bit), 3);
    if (level.getBlockEntity(pos) instanceof ICoverHost host) {
      host.setCover(state, bit);
    }
    return true;
  }
}
