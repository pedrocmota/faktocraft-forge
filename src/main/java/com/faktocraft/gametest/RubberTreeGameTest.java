package com.faktocraft.gametest;

import com.faktocraft.Faktocraft;
import com.faktocraft.common.block.impl.rubber_wood.RubberSapling;
import com.faktocraft.common.registries.ModBlocks;
import com.faktocraft.common.util.BlockStateHelper;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Faktocraft.MODID)
@PrefixGameTestTemplate(false)
public class RubberTreeGameTest {

  private static final String TEMPLATE = "gametest_platform";
  private static final BlockPos ORIGIN = new BlockPos(6, 2, 6);
  private static final int RUNS = 400;
  private static final int CLEAR_RADIUS = 5;
  private static final int CLEAR_HEIGHT = 20;

  private static void clearArea(ServerLevel level, BlockPos origin) {
    for (int x = -CLEAR_RADIUS; x <= CLEAR_RADIUS; x++) {
      for (int z = -CLEAR_RADIUS; z <= CLEAR_RADIUS; z++) {
        for (int y = 0; y <= CLEAR_HEIGHT; y++) {
          level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), 2);
        }
        level.setBlock(origin.offset(x, -1, z), Blocks.GRASS_BLOCK.defaultBlockState(), 2);
      }
    }
  }

  private static final class Stats {
    int trees;
    int placedFailed;
    int noSpot;
    int dryOnly;
    int twoSpots;
    int spotAtGround;
    int[] spotByIndex = new int[16];
    int[] heightCount = new int[16];

    String summary(String label) {
      StringBuilder idx = new StringBuilder();
      for (int i = 0; i < spotByIndex.length; i++) {
        if (spotByIndex[i] > 0) {
          idx.append(i).append('=').append(spotByIndex[i]).append(' ');
        }
      }
      StringBuilder heights = new StringBuilder();
      for (int i = 0; i < heightCount.length; i++) {
        if (heightCount[i] > 0) {
          heights.append(i).append('=').append(heightCount[i]).append(' ');
        }
      }
      return String.format(Locale.ROOT,
          "[RubberTree %s] trees=%d placeFailed=%d noSpot=%d dryOnly=%d twoSpots=%d spotAtGround=%d"
              + " spotIndex{%s} heights{%s}",
          label, trees, placedFailed, noSpot, dryOnly, twoSpots, spotAtGround, idx.toString().trim(),
          heights.toString().trim());
    }
  }

  private static void inspect(ServerLevel level, BlockPos origin, Stats stats) {
    int wet = 0;
    int dry = 0;
    int height = 0;
    boolean ground = false;
    for (int i = 0; i < 16; i++) {
      BlockState state = level.getBlockState(origin.above(i));
      if (!state.is(ModBlocks.RUBBER_LOG)) {
        break;
      }
      height = i + 1;
      boolean isWet = state.getValue(BlockStateHelper.wetProperty);
      boolean isDry = state.getValue(BlockStateHelper.dryProperty);
      if (isWet || isDry) {
        stats.spotByIndex[i]++;
        if (i == 0) {
          ground = true;
        }
      }
      if (isWet) {
        wet++;
      } else if (isDry) {
        dry++;
      }
    }
    stats.trees++;
    stats.heightCount[Math.min(height, 15)]++;
    if (wet + dry == 0) {
      stats.noSpot++;
    }
    if (wet == 0 && dry > 0) {
      stats.dryOnly++;
    }
    if (wet + dry >= 2) {
      stats.twoSpots++;
    }
    if (ground) {
      stats.spotAtGround++;
    }
  }

  private static Stats runFeature(GameTestHelper helper, String featureName) {
    ServerLevel level = helper.getLevel();
    BlockPos origin = helper.absolutePos(ORIGIN);
    ResourceKey<ConfiguredFeature<?, ?>> key = ResourceKey.create(Registries.CONFIGURED_FEATURE,
        new ResourceLocation(Faktocraft.MODID, featureName));
    ConfiguredFeature<?, ?> feature = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE)
        .getOrThrow(key);
    Stats stats = new Stats();
    for (int run = 0; run < RUNS; run++) {
      clearArea(level, origin);
      if (!feature.place(level, level.getChunkSource().getGenerator(), level.getRandom(), origin)) {
        stats.placedFailed++;
        continue;
      }
      inspect(level, origin, stats);
    }
    clearArea(level, origin);
    return stats;
  }

  private static Stats runSapling(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    BlockPos origin = helper.absolutePos(ORIGIN);
    Stats stats = new Stats();
    for (int run = 0; run < RUNS; run++) {
      clearArea(level, origin);
      BlockState sapling = ModBlocks.RUBBER_SAPLING.defaultBlockState();
      level.setBlock(origin, sapling, 2);
      if (!RubberSapling.RUBBER_TREE_GROWER.growTree(level, level.getChunkSource().getGenerator(), origin, sapling,
          level.getRandom())) {
        stats.placedFailed++;
        continue;
      }
      inspect(level, origin, stats);
    }
    clearArea(level, origin);
    return stats;
  }

  @GameTest(template = TEMPLATE, timeoutTicks = 200)
  public static void rubberTreeAlwaysHasResinSpot(GameTestHelper helper) {
    Stats standard = runFeature(helper, "rubber_tree");
    Stats jungle = runFeature(helper, "rubber_tree_jungle");
    Stats sapling = runSapling(helper);
    Faktocraft.LOGGER.info(standard.summary("standard"));
    Faktocraft.LOGGER.info(jungle.summary("jungle"));
    Faktocraft.LOGGER.info(sapling.summary("sapling"));
    helper.assertTrue(standard.trees > 0, "no standard tree placed");
    helper.assertTrue(jungle.trees > 0, "no jungle tree placed");
    helper.assertTrue(sapling.trees > 0, "no sapling tree grown");
    helper.assertTrue(standard.noSpot == 0, standard.noSpot + " standard trees without resin spot");
    helper.assertTrue(jungle.noSpot == 0, jungle.noSpot + " jungle trees without resin spot");
    helper.assertTrue(sapling.noSpot == 0, sapling.noSpot + " sapling trees without resin spot");
    helper.succeed();
  }
}
