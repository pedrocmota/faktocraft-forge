package com.faktocraft.client.model;

import com.faktocraft.common.cover.CoverSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CoverBakedModel implements IDynamicBakedModel {

  public static final ModelProperty<Integer> CLOSED = new ModelProperty<>();

  private final BakedModel original;
  private final boolean drilled;

  public CoverBakedModel(BakedModel original, boolean drilled) {
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

  private static BakedModel coverModel(BlockState cover) {
    return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(cover);
  }

  @NotNull
  @Override
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand,
      @NotNull ModelData data, @Nullable RenderType renderType) {
    BlockState cover = cover(state, data);
    if (cover == null || (renderType == null && !drilled)) {
      return original.getQuads(state, side, rand, data, renderType);
    }
    Integer closed = data.get(CLOSED);
    List<BakedQuad> quads = new ArrayList<>(CoverQuads.get(cover, holes(state, data), closed != null ? closed : 0,
        side, renderType, rand));
    if (!drilled) {
      for (BakedQuad quad : original.getQuads(state, side, rand, data, renderType)) {
        quads.add(CoverQuads.interior(quad));
      }
    }
    return quads;
  }

  @NotNull
  @Override
  public ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state,
      @NotNull ModelData data) {
    if (cover(state, data) == null) {
      return data;
    }
    int holes = holes(state, data);
    int closed = 0;
    for (Direction direction : Direction.values()) {
      int bit = 1 << direction.get3DDataValue();
      BlockPos neighbor = pos.relative(direction);
      if ((holes & bit) != 0 && level.getBlockState(neighbor).isSolidRender(level, neighbor)) {
        closed |= bit;
      }
    }
    return data.derive().with(CLOSED, closed).build();
  }

  @Override
  public ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand,
      @NotNull ModelData data) {
    BlockState cover = cover(state, data);
    if (cover == null) {
      return original.getRenderTypes(state, rand, data);
    }
    ChunkRenderTypeSet coverTypes = coverModel(cover).getRenderTypes(cover, rand, ModelData.EMPTY);
    return drilled ? coverTypes : ChunkRenderTypeSet.union(coverTypes, original.getRenderTypes(state, rand, data));
  }

  @Override
  public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
    BlockState cover = data.get(CoverSupport.COVER);
    return cover != null ? coverModel(cover).getParticleIcon(ModelData.EMPTY) : original.getParticleIcon(data);
  }

  @Override
  public boolean useAmbientOcclusion() {
    return original.useAmbientOcclusion();
  }

  @Override
  public boolean isGui3d() {
    return original.isGui3d();
  }

  @Override
  public boolean usesBlockLight() {
    return original.usesBlockLight();
  }

  @Override
  public boolean isCustomRenderer() {
    return original.isCustomRenderer();
  }

  @Override
  public TextureAtlasSprite getParticleIcon() {
    return original.getParticleIcon(ModelData.EMPTY);
  }

  @Override
  public ItemOverrides getOverrides() {
    return original.getOverrides();
  }

  @Override
  @SuppressWarnings("deprecation")
  public ItemTransforms getTransforms() {
    return original.getTransforms();
  }
}
