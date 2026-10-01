package com.faktocraft.client.model;

import net.minecraft.util.TriState;
import com.faktocraft.common.cover.CoverSupport;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CoverBakedModel implements BlockStateModel {
  private final BlockStateModel original;
  private final boolean drilled;

  public CoverBakedModel(BlockStateModel original, boolean drilled) {
    this.original = original;
    this.drilled = drilled;
  }

  @Nullable
  private BlockState cover(@Nullable BlockState state, ModelData data) {
    BlockState cover = data.get(CoverSupport.COVER);
    if (cover == null || state == null) {
      return null;
    }
    if (!drilled && !CoverSupport.isCovered(state)) {
      return null;
    }
    return cover;
  }

  private int holes(BlockState state, ModelData data) {
    int connections = CoverSupport.connectionMask(state);
    if (drilled) {
      return connections;
    }
    Integer holes = data.get(CoverSupport.COVER_HOLES);
    return connections | (holes != null ? holes : 0);
  }

  private static int closed(BlockAndTintGetter level, BlockPos pos, int holes) {
    int closed = 0;
    for (Direction direction : Direction.values()) {
      int bit = 1 << direction.get3DDataValue();
      BlockPos neighbor = pos.relative(direction);
      if ((holes & bit) != 0 && level.getBlockState(neighbor).isSolidRender()) {
        closed |= bit;
      }
    }
    return closed;
  }

  @Override
  @Deprecated
  public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
    original.collectParts(random, output);
  }

  @Override
  public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
      List<BlockStateModelPart> parts) {
    ModelData data = level.getModelData(pos);
    BlockState cover = cover(state, data);
    if (cover == null) {
      original.collectParts(level, pos, state, random, parts);
      return;
    }
    int holes = holes(state, data);
    QuadCollection coverQuads = CoverQuads.get(cover, holes, closed(level, pos, holes), random);
    QuadCollection.Builder builder = new QuadCollection.Builder().addAll(coverQuads);
    boolean ambientOcclusion = true;
    if (!drilled) {
      List<BlockStateModelPart> originalParts = new ArrayList<>();
      original.collectParts(level, pos, state, random, originalParts);
      for (BlockStateModelPart part : originalParts) {
        ambientOcclusion &= part.ambientOcclusion() != TriState.FALSE;
        for (BakedQuad quad : part.getQuads(null)) {
          builder.addUnculledFace(CoverQuads.interior(quad));
        }
        for (Direction direction : Direction.values()) {
          for (BakedQuad quad : part.getQuads(direction)) {
            builder.addCulledFace(direction, CoverQuads.interior(quad));
          }
        }
      }
    }
    parts.add(new SimpleModelWrapper(builder.build(), ambientOcclusion, coverParticle(level, pos, cover)));
  }

  private static Material.Baked coverParticle(BlockAndTintGetter level, BlockPos pos, BlockState cover) {
    return CoverQuads.coverModel(cover).particleMaterial(level, pos, cover);
  }

  @Override
  @Deprecated
  public Material.Baked particleMaterial() {
    return original.particleMaterial();
  }

  @Override
  public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
    BlockState cover = level.getModelData(pos).get(CoverSupport.COVER);
    return cover != null ? CoverQuads.coverModel(cover).particleMaterial(level, pos, cover)
        : original.particleMaterial(level, pos, state);
  }

  @Override
  @Deprecated
  public int materialFlags() {
    return original.materialFlags();
  }

  @Override
  public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
    BlockState cover = cover(state, level.getModelData(pos));
    if (cover == null) {
      return original.materialFlags(level, pos, state);
    }
    int coverFlags = CoverQuads.coverModel(cover).materialFlags(level, pos, cover);
    return drilled ? coverFlags : coverFlags | original.materialFlags(level, pos, state);
  }

  @Override
  @Nullable
  public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
    return cover(state, level.getModelData(pos)) == null ? original.createGeometryKey(level, pos, state, random)
        : null;
  }
}
