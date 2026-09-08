package com.faktocraft.common.block.impl.forester;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class ForesterAreas {

  private static final Map<ResourceKey<Level>, Set<BlockEntityForester>> ACTIVE = new HashMap<>();

  private ForesterAreas() {
  }

  static void register(BlockEntityForester forester) {
    if (forester.getLevel() instanceof ServerLevel serverLevel) {
      ACTIVE.computeIfAbsent(serverLevel.dimension(), key -> new LinkedHashSet<>()).add(forester);
    }
  }

  static void unregister(BlockEntityForester forester) {
    if (forester.getLevel() instanceof ServerLevel serverLevel) {
      Set<BlockEntityForester> set = ACTIVE.get(serverLevel.dimension());
      if (set != null) {
        set.remove(forester);
      }
    }
  }

  public static boolean covers(ServerLevel level, BlockPos pos) {
    Set<BlockEntityForester> set = ACTIVE.get(level.dimension());
    if (set == null) {
      return false;
    }
    for (BlockEntityForester forester : set) {
      if (!forester.isRemoved() && forester.coversColumn(pos)) {
        return true;
      }
    }
    return false;
  }

  public static void onSaplingGrow(SaplingGrowTreeEvent event) {
    if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
      return;
    }
    Holder<ConfiguredFeature<?, ?>> feature = event.getFeature();
    if (feature == null) {
      return;
    }
    ResourceKey<ConfiguredFeature<?, ?>> replacement = plainOak(feature);
    if (replacement != null && covers(serverLevel, event.getPos())) {
      event.setFeature(replacement);
    }
  }

  @Nullable
  private static ResourceKey<ConfiguredFeature<?, ?>> plainOak(Holder<ConfiguredFeature<?, ?>> feature) {
    if (feature.is(TreeFeatures.FANCY_OAK)) {
      return TreeFeatures.OAK;
    }
    if (feature.is(TreeFeatures.FANCY_OAK_BEES) || feature.is(TreeFeatures.FANCY_OAK_BEES_005)) {
      return TreeFeatures.OAK_BEES_005;
    }
    if (feature.is(TreeFeatures.FANCY_OAK_BEES_002)) {
      return TreeFeatures.OAK_BEES_002;
    }
    if (feature.is(TreeFeatures.FANCY_OAK_BEES_0002)) {
      return TreeFeatures.OAK_BEES_0002;
    }
    return null;
  }

  public static void onLevelUnload(LevelEvent.Unload event) {
    if (event.getLevel() instanceof ServerLevel serverLevel) {
      ACTIVE.remove(serverLevel.dimension());
    }
  }
}
